terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

data "aws_ami" "ubuntu_2204" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

locals {
  # 운영 모드:
  # - active: EC2 실행 + RDS 운영
  # - sleep: EC2 중지 + RDS 운영(짧게 닫을 때)
  # - parked: EC2 중지 + RDS 삭제(스냅샷에서 복원)
  ec2_desired_state   = var.deployment_mode == "active" ? "running" : "stopped"
  manage_rds_instance = var.deployment_mode != "parked"

  # restore_db_from_snapshot=true 인 경우에만 스냅샷 복원 값을 사용합니다.
  restore_rds_from_snapshot = var.restore_db_from_snapshot && trimspace(var.restore_db_snapshot_identifier) != ""

  # final_snapshot_identifier 는 RDS 삭제 시점에 사용되므로 기본값을 두되,
  # parked 모드에서는 사용자가 명시적으로 이름을 주는 것을 권장합니다.
  effective_parked_db_final_snapshot_identifier = (
    trimspace(var.parked_db_final_snapshot_identifier) != ""
    ? trimspace(var.parked_db_final_snapshot_identifier)
    : "${var.project_name}-parked-final-snapshot"
  )
}

check "parked_mode_requires_snapshot_name" {
  assert {
    condition     = var.deployment_mode != "parked" || trimspace(var.parked_db_final_snapshot_identifier) != ""
    error_message = "parked 모드에서는 RDS 복원용 최종 스냅샷 이름(parked_db_final_snapshot_identifier)을 반드시 지정해야 합니다."
  }
}

check "restore_mode_requires_snapshot_name" {
  assert {
    condition     = !var.restore_db_from_snapshot || trimspace(var.restore_db_snapshot_identifier) != ""
    error_message = "restore_db_from_snapshot=true 일 때는 restore_db_snapshot_identifier 값을 반드시 지정해야 합니다."
  }
}

resource "aws_security_group" "ec2_sg" {
  name   = "${var.project_name}-ec2-sg"
  vpc_id = data.aws_vpc.default.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    # SSH는 관리자 PC에서만 접속 가능하도록 제한합니다.
    # 배포는 self-hosted runner가 서버 내부에서 처리하므로 전체 공개가 필요 없습니다.
    description = "SSH_ADMIN_ONLY"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }

  egress {
    description = "ALL OUT"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "rds_sg" {
  name   = "${var.project_name}-rds-sg"
  vpc_id = data.aws_vpc.default.id

  ingress {
    description     = "MariaDB from EC2 only"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2_sg.id]
  }

  egress {
    description = "ALL OUT"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_subnet_group" "rds_subnets" {
  name       = "${var.project_name}-rds-subnets"
  subnet_ids = data.aws_subnets.default.ids
}

resource "aws_db_instance" "rds" {
  count = local.manage_rds_instance ? 1 : 0

  identifier = "${var.project_name}-db"

  engine         = local.restore_rds_from_snapshot ? null : "mariadb"
  engine_version = local.restore_rds_from_snapshot ? null : var.db_engine_version

  instance_class         = var.db_instance_class
  allocated_storage      = local.restore_rds_from_snapshot ? null : var.db_allocated_storage
  storage_type           = "gp3"
  db_name                = local.restore_rds_from_snapshot ? null : var.db_name
  username               = local.restore_rds_from_snapshot ? null : var.db_username
  password               = local.restore_rds_from_snapshot ? null : var.db_password
  port                   = 3306
  snapshot_identifier    = local.restore_rds_from_snapshot ? trimspace(var.restore_db_snapshot_identifier) : null
  db_subnet_group_name   = aws_db_subnet_group.rds_subnets.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  publicly_accessible = false
  # parked 모드로 전환할 때는 이 최종 스냅샷으로 다시 열 수 있습니다.
  skip_final_snapshot       = false
  final_snapshot_identifier = local.effective_parked_db_final_snapshot_identifier
  delete_automated_backups  = true
  copy_tags_to_snapshot     = true
  deletion_protection       = false

  lifecycle {
    # 이미 복원된 RDS 가 살아있는 동안에는 snapshot_identifier 변경 때문에
    # 불필요한 교체가 일어나지 않도록 막습니다.
    ignore_changes = [snapshot_identifier]
  }
}

resource "aws_iam_role" "ec2_ssm_role" {
  name = "${var.project_name}-ec2-ssm-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ec2_ssm_core" {
  role       = aws_iam_role.ec2_ssm_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2_ssm_profile" {
  name = "${var.project_name}-ec2-ssm-profile"
  role = aws_iam_role.ec2_ssm_role.name
}

resource "aws_instance" "app" {
  ami                    = data.aws_ami.ubuntu_2204.id
  instance_type          = var.ec2_instance_type
  key_name               = var.ec2_key_name
  vpc_security_group_ids = [aws_security_group.ec2_sg.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_ssm_profile.name

  user_data = file("${path.module}/userdata.sh")

  # 메타데이터 토큰을 강제해서 기본 보안 수준을 높입니다.
  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  tags = {
    Name = "${var.project_name}-ec2"
  }
}

# 운영 안정화를 위해 EC2 공인 IP를 고정합니다.
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  tags = {
    Name = "${var.project_name}-ec2-eip"
  }
}

# 하드웨어 레벨 상태 점검 실패 시 자동 복구(recover)를 시도합니다.
# 운영을 닫아둘 때도 EC2 자체는 보존하고, Terraform 이 실행/중지 상태만 관리합니다.
# 이렇게 하면 root 디스크와 self-hosted runner 설정을 그대로 살려서 다시 열기 쉽습니다.
resource "aws_ec2_instance_state" "app" {
  instance_id = aws_instance.app.id
  state       = local.ec2_desired_state

  depends_on = [aws_eip.app]
}

resource "aws_cloudwatch_metric_alarm" "ec2_auto_recover" {
  alarm_name          = "${var.project_name}-ec2-auto-recover"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed_System"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = 1
  alarm_description   = "EC2 시스템 상태 점검 실패 시 자동 복구"
  treat_missing_data  = "notBreaching"

  dimensions = {
    InstanceId = aws_instance.app.id
  }

  alarm_actions = ["arn:aws:automate:${var.region}:ec2:recover"]
}

# 인스턴스 레벨 상태 점검 실패 시 자동 재부팅(reboot)을 시도합니다.
resource "aws_cloudwatch_metric_alarm" "ec2_auto_reboot" {
  alarm_name          = "${var.project_name}-ec2-auto-reboot"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed_Instance"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = 1
  alarm_description   = "EC2 인스턴스 상태 점검 실패 시 자동 재부팅"
  treat_missing_data  = "notBreaching"

  dimensions = {
    InstanceId = aws_instance.app.id
  }

  alarm_actions = ["arn:aws:automate:${var.region}:ec2:reboot"]
}
