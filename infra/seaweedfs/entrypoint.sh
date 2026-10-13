#!/bin/sh
# Sobe o SeaweedFS com o gateway S3 para desenvolvimento.
# O s3.json (chaves de acesso) é gerado aqui a partir das variáveis de ambiente, para o segredo
# morar só no .env e nunca num arquivo versionado.
set -eu

: "${STORAGE_ACCESS_KEY:?defina STORAGE_ACCESS_KEY no .env}"
: "${STORAGE_SECRET_KEY:?defina STORAGE_SECRET_KEY no .env}"
: "${STORAGE_BUCKET:=festconnect-midias}"

mkdir -p /etc/seaweedfs
cat > /etc/seaweedfs/s3.json <<EOF
{
  "identities": [
    {
      "name": "festconnect-api",
      "credentials": [
        { "accessKey": "${STORAGE_ACCESS_KEY}", "secretKey": "${STORAGE_SECRET_KEY}" }
      ],
      "actions": ["Read:${STORAGE_BUCKET}", "List:${STORAGE_BUCKET}", "Write:${STORAGE_BUCKET}", "Tagging:${STORAGE_BUCKET}"]
    },
    {
      "name": "anonymous",
      "actions": ["Read:${STORAGE_BUCKET}"]
    }
  ]
}
EOF

# A chave da API só lê, lista e grava NESTE bucket (sem Admin): não cria nem apaga buckets.
# O navegador lê as fotos direto daqui, sem chave (identidade "anonymous", só leitura).
weed server -dir=/data -s3 -s3.port=8333 -s3.config=/etc/seaweedfs/s3.json &
PID_SERVIDOR=$!
trap 'kill -TERM "$PID_SERVIDOR"' TERM INT

# Cria o bucket pelo shell administrativo do próprio SeaweedFS, assim que ele estiver pronto.
until echo "s3.bucket.list" | weed shell >/dev/null 2>&1; do sleep 1; done
if ! echo "s3.bucket.list" | weed shell 2>/dev/null | grep -q "${STORAGE_BUCKET}"; then
  echo "s3.bucket.create -name ${STORAGE_BUCKET}" | weed shell
  echo "bucket ${STORAGE_BUCKET} criado"
fi

wait "$PID_SERVIDOR"
