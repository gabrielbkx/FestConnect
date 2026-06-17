#!/bin/bash
# Orquestra o deploy blue-green NA EC2. Enviado e executado pelo deploy.sh.
# Sobe a nova versao numa porta ociosa, espera ela ficar pronta e so entao
# vira o trafego do Nginx. Se a nova versao nao subir, aborta sem derrubar o site.
set -euo pipefail

UPSTREAM_FILE=/etc/nginx/conf.d/festconnect-upstream.conf
HEALTH_PATH=/actuator/health
JAR_FINAL=/home/ec2-user/festconnect.jar
JAR_TMP=/home/ec2-user/festconnect.new.jar
MAX_WAIT=90  # segundos de espera ate a nova instancia responder

# 0. Move atomico do jar novo. A versao antiga mantem o jar anterior aberto via
#    inode; a nova instancia (passo 2) le este jar novo. Sem colisao de bytecode.
if [ -f "$JAR_TMP" ]; then
  mv -f "$JAR_TMP" "$JAR_FINAL"
fi

# 1. Descobre a porta ativa hoje e escolhe a outra para a nova versao
CURRENT_PORT=$(grep -oP '127\.0\.0\.1:\K[0-9]+' "$UPSTREAM_FILE")
if [ "$CURRENT_PORT" = "8080" ]; then
  NEW_PORT=8081
else
  NEW_PORT=8080
fi
echo "==> Porta ativa: $CURRENT_PORT | Subindo nova versao em: $NEW_PORT"

# 2. Sobe a nova instancia com o jar recem-enviado (restart garante jar novo)
sudo systemctl restart "festconnect@$NEW_PORT"

# 3. Espera a nova instancia comecar a responder. Qualquer HTTP (ex: 401)
#    significa que o Tomcat subiu e o app esta servindo. 000 = ainda nao subiu.
echo "==> Aguardando festconnect@$NEW_PORT ficar pronta (ate ${MAX_WAIT}s)..."
READY=0
for i in $(seq 1 "$MAX_WAIT"); do
  CODE=$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:$NEW_PORT$HEALTH_PATH" || true)
  if [ -n "$CODE" ] && [ "$CODE" != "000" ]; then
    echo "==> Pronta! (HTTP $CODE apos ${i}s)"
    READY=1
    break
  fi
  sleep 1
done

# 4. Se nao subiu, aborta SEM tocar no Nginx. O site segue na versao anterior.
if [ "$READY" -ne 1 ]; then
  echo "!! festconnect@$NEW_PORT NAO ficou pronta em ${MAX_WAIT}s. Abortando."
  echo "!! O site continua no ar na porta $CURRENT_PORT (versao anterior)."
  sudo systemctl stop "festconnect@$NEW_PORT"
  exit 1
fi

# 5. Vira o trafego para a nova porta (reload graceful, sem derrubar conexao)
echo "==> Virando o Nginx para a porta $NEW_PORT..."
sudo sed -i "s#127.0.0.1:$CURRENT_PORT#127.0.0.1:$NEW_PORT#" "$UPSTREAM_FILE"
if ! sudo nginx -t; then
  echo "!! nginx -t falhou. Revertendo upstream e abortando."
  sudo sed -i "s#127.0.0.1:$NEW_PORT#127.0.0.1:$CURRENT_PORT#" "$UPSTREAM_FILE"
  sudo systemctl stop "festconnect@$NEW_PORT"
  exit 1
fi
sudo systemctl reload nginx

# 6. Garante que a porta ativa suba sozinha num reboot; desativa a antiga
sudo systemctl enable "festconnect@$NEW_PORT"
sudo systemctl disable "festconnect@$CURRENT_PORT"

# 7. Desliga a versao antiga
echo "==> Parando a versao antiga (porta $CURRENT_PORT)..."
sudo systemctl stop "festconnect@$CURRENT_PORT"

echo "==> Deploy concluido. Trafego agora na porta $NEW_PORT."
