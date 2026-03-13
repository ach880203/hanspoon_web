output "ec2_public_ip" { value = aws_eip.app.public_ip }
output "ec2_public_dns" { value = aws_instance.app.public_dns }
output "ec2_elastic_ip" { value = aws_eip.app.public_ip }
output "deployment_mode" { value = var.deployment_mode }
output "ec2_desired_state" { value = aws_ec2_instance_state.app.state }
output "rds_endpoint" { value = local.manage_rds_instance ? aws_db_instance.rds[0].address : null }
output "rds_port" { value = local.manage_rds_instance ? aws_db_instance.rds[0].port : null }
output "rds_restore_snapshot_identifier" {
  value = var.restore_db_from_snapshot ? trimspace(var.restore_db_snapshot_identifier) : null
}
output "rds_final_snapshot_identifier" {
  value = local.effective_parked_db_final_snapshot_identifier
}
