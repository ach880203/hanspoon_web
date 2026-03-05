variable "project_name" {
  type    = string
  default = "hanspoon"
}

variable "region" {
  type    = string
  default = "ap-northeast-2"
}

variable "ec2_instance_type" {
  type    = string
  default = "t3.micro"
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