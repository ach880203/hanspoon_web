output "managed_secret_names" {
  description = "Terraform으로 반영한 GitHub Actions 시크릿 이름 목록"
  value       = sort(keys(github_actions_secret.managed))
}
