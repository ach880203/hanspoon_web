variable "github_owner" {
  description = "GitHub 저장소 소유자"
  type        = string
  default     = "ach880203"
}

variable "github_repository" {
  description = "GitHub Actions 시크릿을 관리할 저장소 이름"
  type        = string
  default     = "hanspoon_web"
}

variable "actions_secrets" {
  description = "Terraform으로 관리할 GitHub Actions 시크릿 목록"
  type        = map(string)
  sensitive   = true
}
