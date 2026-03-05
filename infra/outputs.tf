output "ec2_public_ip" { value = aws_eip.app.public_ip }
output "ec2_public_dns" { value = aws_instance.app.public_dns }
output "ec2_elastic_ip" { value = aws_eip.app.public_ip }
output "rds_endpoint" { value = aws_db_instance.rds.address }
output "rds_port" { value = aws_db_instance.rds.port }
