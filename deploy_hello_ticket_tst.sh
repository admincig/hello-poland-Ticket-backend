#!/usr/bin/env bash
set -Eeuo pipefail

WAR="/home/debian/hp_ticket_backend/helloticket.war"
CONFIG="/srv/hpt/tst/config/hpt-tst.config.properties"
OOM="/home/debian/hp_ticket_backend/oom"

test -s "$WAR" || { echo "Brak WAR: $WAR" >&2; exit 1; }
test -f "$CONFIG" || { echo "Brak konfiguracji: $CONFIG" >&2; exit 1; }
docker image inspect hpt-backend:prev-20251218_162948 >/dev/null
docker network inspect hpl_tst_network >/dev/null
docker volume inspect HELLO_DMS_TST >/dev/null

mkdir -p "$OOM"
chmod 777 "$OOM"

docker rm -f hello_ticket_tst >/dev/null 2>&1 || true

docker run -d --name hello_ticket_tst \
  --restart unless-stopped \
  --network hpl_tst_network \
  -p 127.0.0.1:8190:8080 \
  --memory=6g --memory-swap=6g \
  -v HELLO_DMS_TST:/DMS \
  -v "$WAR:/opt/jboss/wildfly/standalone/deployments/helloticket.war:ro" \
  -v "$CONFIG:/opt/jboss/wildfly/standalone/deployments/local.runtime.properties:ro" \
  -v "$OOM:/oom" \
  -v /etc/localtime:/etc/localtime:ro \
  -e TZ=Europe/Warsaw \
  -e JAVA_TOOL_OPTIONS='-Xms1g -Xmx4g -Dpool.entry.date.repairer.enabled=false -Dlocal.runtime.properties=/opt/jboss/wildfly/standalone/deployments/local.runtime.properties -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/oom -XX:+ExitOnOutOfMemoryError' \
  hpt-backend:prev-20251218_162948 >/dev/null

echo "Oczekiwanie na HT TST..."

for _ in $(seq 1 90); do
  DEPLOYMENT_INFO="$(
    docker exec hello_ticket_tst \
      /opt/jboss/wildfly/bin/jboss-cli.sh \
      --connect --commands='deployment-info' 2>/dev/null || true
  )"

  if printf '%s\n' "$DEPLOYMENT_INFO" |
    grep -Eq '^helloticket\.war[[:space:]].*[[:space:]]OK[[:space:]]*$'
  then
    printf '%s\n' "$DEPLOYMENT_INFO"
    curl -sS -o /dev/null -w 'HTTP_STATUS=%{http_code}\n' \
      http://127.0.0.1:8190/helloticket/ || true
    docker ps --filter 'name=^/hello_ticket_tst$'
    exit 0
  fi

  sleep 2
done

echo "HT TST nie uruchomił się poprawnie." >&2
docker logs --tail 200 hello_ticket_tst 2>&1 || true
exit 1
