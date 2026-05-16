## Этап 0 — Снос всего лишнего
## 0.1 Удалить виртуалки 
##  Сначала погасить всё запущенное


for vm in ansible02_consul_server ansible02_api ansible02_manager2 ansible02_db \
          src_manager src_node01 src_node02 \
          do-h-07-manager01 do-h-07-worker01 do-h-07-worker02; do
    sudo virsh destroy "$vm" 2>/dev/null
    sudo virsh undefine "$vm" --remove-all-storage 2>/dev/null
done

# Проверка
sudo virsh list --all


## 0.2 Удалить осиротевшие тома (диски виртуалок)

# Посмотреть что осталось
sudo virsh vol-list --pool default

# Если остались старые .img файлы — удалить (заменяй имена на реальные):
sudo virsh vol-delete --pool default ИМЯ_ОБРАЗА.img


## 0.3 Проверка ресурсов
df -h /var/lib/libvirt/images   # должно быть свободно 30+ ГБ
free -h                          # свободной памяти 50+ ГБ
vagrant box list                 # должна быть строка my-ubuntu2204
sudo virsh net-list --all        # ищем do-h-07-net

















































4.9 Доступность google.com - снова  упал полсе этих команд :

# 1. Все сервисы monitoring 

ssh manager01 "sudo docker stack services monitoring"

# 2. cAdvisor

for NODE in manager01 worker01 worker02; do

    echo "=== $NODE cadvisor ==="

    ssh $NODE "sudo docker ps --filter name=cadvisor --format '{{.Status}}'"

done

# 3. Сколько контейнеров видит cAdvisor в Prometheus

echo ""

echo "=== Containers count ==="

curl -s 'http://192.168.56.10:9090/api/v1/query' \

    --data-urlencode 'query=count(container_last_seen{container_label_com_docker_swarm_service_name!=""})' \

    | python3 -m json.tool | head -10

# 4. Loki — реальные логи app-сервисов

echo ""

echo "=== Loki: app_booking logs ==="

NOW=$(date +%s)000000000

MIN_AGO=$(date -d '2 minutes ago' +%s)000000000

curl -s --max-time 15 -G "http://192.168.56.10:3100/loki/api/v1/query_range" \

    --data-urlencode 'query={service="app_booking-service"}' \

    --data-urlencode "start=$MIN_AGO" \

    --data-urlencode "end=$NOW" \

    --data-urlencode 'limit=3' | python3 -m json.tool | head -25







и снова ожи при этотой : 

# 5. Google probe

echo ""

echo "=== probe_success ==="

curl -s 'http://192.168.56.10:9090/api/v1/query' \

    --data-urlencode 'query=probe_success' \

    | python3 -c "

import sys, json

r = json.load(sys.stdin)['data']['result']

for s in r:

    print(s['metric'].get('instance','?'), '=', s['value'][1])

"












# 1. Все сервисы monitoring 
ssh manager01 "sudo docker stack services monitoring"

# 2. cAdvisor
for NODE in manager01 worker01 worker02; do
    echo "=== $NODE cadvisor ==="
    ssh $NODE "sudo docker ps --filter name=cadvisor --format '{{.Status}}'"
done

# 3. Сколько контейнеров видит cAdvisor в Prometheus
echo ""
echo "=== Containers count ==="
curl -s 'http://192.168.56.10:9090/api/v1/query' \
    --data-urlencode 'query=count(container_last_seen{container_label_com_docker_swarm_service_name!=""})' \
    | python3 -m json.tool | head -10

# 4. Loki — реальные логи app-сервисов
echo ""
echo "=== Loki: app_booking logs ==="
NOW=$(date +%s)000000000
MIN_AGO=$(date -d '2 minutes ago' +%s)000000000
curl -s --max-time 15 -G "http://192.168.56.10:3100/loki/api/v1/query_range" \
    --data-urlencode 'query={service="app_booking-service"}' \
    --data-urlencode "start=$MIN_AGO" \
    --data-urlencode "end=$NOW" \
    --data-urlencode 'limit=3' | python3 -m json.tool | head -25





























# 5. Google probe   - ВВОДИ ЭТО ЧТО БЫ ЗАРАБОТАЛО!!!!!
echo ""
echo "=== probe_success ==="
curl -s 'http://192.168.56.10:9090/api/v1/query' \
    --data-urlencode 'query=probe_success' \
    | python3 -c "
import sys, json
r = json.load(sys.stdin)['data']['result']
for s in r:
    print(s['metric'].get('instance','?'), '=', s['value'][1])
"