terraform {
  required_version = ">= 1.5.0"

  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
  }
}

provider "github" {
  owner = var.github_owner
}

resource "github_actions_secret" "managed" {
  for_each = nonsensitive(var.actions_secrets)

  repository      = var.github_repository
  secret_name     = each.key
  plaintext_value = each.value
}
