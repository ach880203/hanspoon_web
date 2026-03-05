#!/bin/bash
set -e

sudo apt-get update -y
sudo apt-get install -y nginx unzip curl

# Java 21(Temurin)
sudo apt-get install -y wget apt-transport-https ca-certificates gnupg
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb jammy main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt-get update -y
sudo apt-get install -y temurin-21-jdk

# 배포 폴더
sudo mkdir -p /opt/hanspoon/current/backend
sudo mkdir -p /opt/hanspoon/current/frontend
sudo mkdir -p /opt/hanspoon/env
sudo mkdir -p /opt/hanspoon/data/images
sudo chown -R ubuntu:ubuntu /opt/hanspoon

# systemd 서비스(백엔드)
sudo tee /etc/systemd/system/hanspoon-backend.service > /dev/null <<'EOF'
[Unit]
Description=hanspoon backend
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/opt/hanspoon/current/backend
EnvironmentFile=/opt/hanspoon/env/backend.env
ExecStart=/usr/bin/java -jar /opt/hanspoon/current/backend/app.jar
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable hanspoon-backend.service

# 백엔드 헬스체크 실패 시 자동 재시작(운영 안정화)
sudo tee /usr/local/bin/hanspoon-healthcheck.sh > /dev/null <<'EOF'
#!/bin/bash
set -euo pipefail

HEALTH_URL="http://127.0.0.1/api/actuator/health"
SERVICE_NAME="hanspoon-backend"

if curl -fsS --max-time 5 "${HEALTH_URL}" >/dev/null 2>&1; then
  exit 0
fi

if ! systemctl is-active --quiet "${SERVICE_NAME}"; then
  systemctl start "${SERVICE_NAME}"
  exit 0
fi

systemctl restart "${SERVICE_NAME}"
EOF
sudo chmod +x /usr/local/bin/hanspoon-healthcheck.sh

sudo tee /etc/systemd/system/hanspoon-healthcheck.service > /dev/null <<'EOF'
[Unit]
Description=Hanspoon backend healthcheck and auto-restart
After=network.target

[Service]
Type=oneshot
ExecStart=/usr/local/bin/hanspoon-healthcheck.sh
EOF

sudo tee /etc/systemd/system/hanspoon-healthcheck.timer > /dev/null <<'EOF'
[Unit]
Description=Run hanspoon backend healthcheck every minute

[Timer]
OnBootSec=2min
OnUnitActiveSec=1min
Persistent=true

[Install]
WantedBy=timers.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now hanspoon-healthcheck.timer

# nginx: 프런트 정적 + /api -> 8080 프록시
sudo tee /etc/nginx/sites-available/hanspoon > /dev/null <<'EOF'
server {
  listen 80;
  server_name _;

  root /opt/hanspoon/current/frontend;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location /api/ {
    proxy_pass http://127.0.0.1:8080/;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }

  location /images/ {
    alias /opt/hanspoon/data/images/;
    try_files $uri =404;
  }

  location = /api/actuator/health {
    proxy_pass http://127.0.0.1:8080/actuator/health;
  }
}
EOF

sudo ln -sf /etc/nginx/sites-available/hanspoon /etc/nginx/sites-enabled/hanspoon
sudo rm -f /etc/nginx/sites-enabled/default

sudo nginx -t
sudo systemctl restart nginx
