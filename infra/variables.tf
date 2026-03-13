variable "project_name" {
  type    = string
  default = "hanspoon"
}

variable "deployment_mode" {
  type        = string
  default     = "active"
  description = "active=전체 운영, sleep=EC2만 중지, parked=EC2 중지 + RDS 삭제(스냅샷 복원용)"

  validation {
    condition     = contains(["active", "sleep", "parked"], var.deployment_mode)
    error_message = "deployment_mode 는 active, sleep, parked 중 하나여야 합니다."
  }
}

variable "region" {
  type    = string
  default = "ap-northeast-2"
}

variable "ec2_instance_type" {
  type    = string
  default = "t3.small"
}

variable "ec2_key_name" {
  type        = string
  description = "AWS 콘솔에 만든 키페어 이름"
}

variable "allowed_ssh_cidr" {
  type        = string
  description = "내 IP/32 (예: 1.2.3.4/32)"
}

variable "app_port" {
  type    = number
  default = 8080
}

variable "db_engine_version" {
  type    = string
  default = "10.11"
}

variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "db_allocated_storage" {
  type    = number
  default = 20
}

variable "db_name" {
  type    = string
  default = "hanspoon"
}

variable "db_username" {
  type    = string
  default = "hanspoon"
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "parked_db_final_snapshot_identifier" {
  type        = string
  default     = ""
  description = "parked 모드로 닫을 때 생성할 최종 RDS 스냅샷 이름"
}

variable "restore_db_from_snapshot" {
  type        = bool
  default     = false
  description = "true 면 restore_db_snapshot_identifier 스냅샷으로 RDS 를 복원합니다."
}

variable "restore_db_snapshot_identifier" {
  type        = string
  default     = ""
  description = "restore_db_from_snapshot=true 일 때 복원할 RDS 스냅샷 이름"
}
