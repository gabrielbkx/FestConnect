#!/bin/bash
set -e

EC2_HOST="ec2-user@15.228.203.131"
EC2_KEY="$HOME/.ssh/festconnect-key.pem"
JAR_LOCAL="target/festconnect-0.0.1-SNAPSHOT.jar"
JAR_REMOTE="/home/ec2-user/festconnect.jar"

echo "==> [1/3] Build do JAR..."
mvn package -DskipTests -q

echo "==> [2/3] Enviando JAR para a EC2..."
scp -i "$EC2_KEY" "$JAR_LOCAL" "$EC2_HOST:$JAR_REMOTE"

echo "==> [3/3] Reiniciando o backend..."
ssh -i "$EC2_KEY" "$EC2_HOST" "sudo systemctl restart festconnect && sudo systemctl is-active festconnect"

echo ""
echo "Deploy concluido."
