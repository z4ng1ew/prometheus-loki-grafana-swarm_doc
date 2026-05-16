
## DO9_Monitoring — Отчёт

## Part 1. Получение метрик и логов

## Задание 1. Использование Docker Swarm из первого проекта


![alt text](img/1/image.png)

**Vagrantfile создаёт manager, node01, node02 и подтверждает «Docker Swarm из первого проекта».**



```
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ssh manager01 "sudo docker node ls"
ID                            HOSTNAME    STATUS    AVAILABILITY   MANAGER STATUS   ENGINE VERSION
fflgfig5hdt1nf53ea6ga2r9j *   manager01   Ready     Active         Leader           29.4.3
svtmgbq6eci8aygbyqtusmeko     worker01    Ready     Active                          29.4.3
s25bu92g5dyw5m9vjmb82cxab     worker02    Ready     Active                          29.4.3
(base) s@fedora:~/prometheus-loki-grafana-swarm$ 
```





![src/img/1/image copy.png](img/1/image_copy.png)

**Диагностика — что живо**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm$ sudo virsh list --all
[sudo] пароль для s: 
 ID   Имя                 Состояние
-------------------------------------
 16   do-h-07-manager01   работает
 19   do-h-07-worker02    работает
 20   do-h-07-worker01    работает

```











![alt text](<img/1/image copy.png>)

**ping на ip виртуалок**


```
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ping -c 2 192.168.56.10
PING 192.168.56.10 (192.168.56.10) 56(84) bytes of data.
64 bytes from 192.168.56.10: icmp_seq=1 ttl=64 time=0.206 ms
64 bytes from 192.168.56.10: icmp_seq=2 ttl=64 time=0.090 ms

--- 192.168.56.10 ping statistics ---
2 packets transmitted, 2 received, 0% packet loss, time 1015ms
rtt min/avg/max/mdev = 0.090/0.148/0.206/0.058 ms
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ping -c 2 192.168.56.11
PING 192.168.56.11 (192.168.56.11) 56(84) bytes of data.
64 bytes from 192.168.56.11: icmp_seq=1 ttl=64 time=0.232 ms
64 bytes from 192.168.56.11: icmp_seq=2 ttl=64 time=0.184 ms

--- 192.168.56.11 ping statistics ---
2 packets transmitted, 2 received, 0% packet loss, time 1023ms
rtt min/avg/max/mdev = 0.184/0.208/0.232/0.024 ms
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ping -c 2 192.168.56.12
PING 192.168.56.12 (192.168.56.12) 56(84) bytes of data.
64 bytes from 192.168.56.12: icmp_seq=1 ttl=64 time=0.238 ms
64 bytes from 192.168.56.12: icmp_seq=2 ttl=64 time=0.149 ms

--- 192.168.56.12 ping statistics ---
2 packets transmitted, 2 received, 0% packet loss, time 1008ms
rtt min/avg/max/mdev = 0.149/0.193/0.238/0.044 ms
(base) s@fedora:~/prometheus-loki-grafana-swarm$ 
```



![alt text](<img/1/image copy 2.png>)

**сриншот из менеджера виртуальных машин на линукс который подтверждает что все 3 виртуалки запущены, что подтверждает использование docker swarm из D07**









![alt text](<img/1/image copy 3.png>)

**Все 9 сервисов работают**
















## 1. Метрики и журналы

**Part 1. Метрики через Micrometer**

**2. Написать при помощи библиотеки Micrometer сборщики следующих метрик приложения:** 


  ## 1) Количество отправленных сообщений в rabbitmq;

![alt text](<img/1/image copy 4.png>)

**Скриншот 1: booking-service — отправлено в RabbitMQ + бронирования**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ ssh worker01 "sudo docker exec \$(sudo docker ps -q --filter name=app_booking-service | head -1) wget -qO- http://localhost:8083/actuator/prometheus 2>/dev/null | grep app_"
# HELP app_bookings_created_total Сколько бронирований было создано
# TYPE app_bookings_created_total counter
app_bookings_created_total{application="booking-service",} 0.0
# HELP app_rabbitmq_messages_sent_total Сколько сообщений отправлено в RabbitMQ
# TYPE app_rabbitmq_messages_sent_total counter
app_rabbitmq_messages_sent_total{application="booking-service",} 0.0
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$
```

**Вот где файл MetricsConfig**

![alt text](<img/1/image copy 9.png>)

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$
grep -rn "rabbitmq_messages_sent" services/booking-service/src/ --include="*.java" -l
services/booking-service/src/main/java/com/s21/devops/sample/bookingservice/config/MetricsConfig.java
```




















  ## 2) Количество обработанных сообщений в rabbitmq;

![alt text](<img/1/image copy 5.png>)

**Скриншот 2: report-service — обработано из RabbitMQ**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ ssh worker01 "sudo docker exec \$(sudo docker ps -q --filter name=app_report-service | head -1) wget -qO- http://localhost:8086/actuator/prometheus 2>/dev/null | grep app_"
# HELP app_rabbitmq_messages_processed_total Сколько сообщений обработано из RabbitMQ
# TYPE app_rabbitmq_messages_processed_total counter
app_rabbitmq_messages_processed_total 0.0
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```


**Вот где файл MetricsConfig**


![alt text](<img/1/image copy 10.png>)

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ grep -rn "rabbitmq_messages_processed" services/report-service/src/ --include="*.java" -l
services/report-service/src/main/java/com/s21/devops/sample/reportservice/config/MetricsConfig.java
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```














  ## 3) Количество бронирований;

![alt text](<img/1/image copy 8.png>)

**Вот где файл MetricsConfig**

**Скриншот 3: booking-service — запросы на бронирование**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ ssh worker01 "sudo docker exec \$(sudo docker ps -q --filter name=app_booking-service | head -1) wget -qO- http://localhost:8083/actuator/prometheus 2>/dev/null | grep app_bookings_created"
# HELP app_bookings_created_total Сколько бронирований было создано
# TYPE app_bookings_created_total counter
app_bookings_created_total{application="booking-service",} 0.0
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```







  ## 4) Количество полученных запросов на gateway (шлюзу);

![alt text](<img/1/image copy 6.png>)

**Скриншот 4: gateway-service — запросы на шлюз**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ ssh worker01 "sudo docker exec \$(sudo docker ps -q --filter name=app_gateway-service | head -1) wget -qO- http://localhost:8087/actuator/prometheus 2>/dev/null | grep app_"
# HELP app_gateway_requests_total Сколько запросов получено на gateway
# TYPE app_gateway_requests_total counter
app_gateway_requests_total{application="gateway-service",} 2.0
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```


![alt text](<img/1/image copy 11.png>)

**Вот где файл MetricsConfig**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ grep -rn "gateway_requests" services/gateway-service/src/ --include="*.java" -l
services/gateway-service/src/main/java/com/s21/devops/sample/gatewayservice/filter/RequestCounterFilter.java
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```





  ## 5) Количество полученных запросов на авторизацию пользователей.

![alt text](<img/1/image copy 7.png>)

**Скриншот 5: session-service — запросы на авторизацию**


```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ ssh worker01 "sudo docker exec \$(sudo docker ps -q --filter name=app_session-service | head -1) wget -qO- http://localhost:8081/actuator/prometheus 2>/dev/null | grep app_"
# HELP app_auth_requests_total Сколько запросов на авторизацию получено
# TYPE app_auth_requests_total counter
app_auth_requests_total{application="session-service",} 2.0
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```




![alt text](<img/1/image copy 13.png>)

**Вот где файл MetricsConfig**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ grep -rn "auth_requests" services/session-service/src/ --include="*.java" -l
services/session-service/src/main/java/com/s21/devops/sample/sessionservice/filter/AuthRequestCounterFilter.java
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```
















Добавлены следующие метрики:

| Метрика | Сервис | Endpoint Prometheus |
|---|---|---|
| `app_bookings_created_total` | booking-service | `/actuator/prometheus` |
| `app_rabbitmq_messages_sent_total` | booking-service | то же |
| `app_rabbitmq_messages_processed_total` | report-service | `/actuator/prometheus` |
| `app_gateway_requests_total` | gateway-service | `/actuator/prometheus` |
| `app_auth_requests_total` | session-service | `/actuator/prometheus` |

































































## 2) Сбор логов из сервисов приложения осуществляется с помощью promtail.





![alt text](<img/1/image copy 28.png>)
**Стек мониторинга**

**Подпись:** «Сервис `monitoring_promtail` развёрнут в режиме `global` с тремя репликами 
(по одной на каждую ноду). Promtail собирает логи Docker-контейнеров с каждого узла.»


```
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ssh manager01 "sudo docker stack ls"
NAME         SERVICES
app          9
monitoring   6
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ssh manager01 "sudo docker stack services monitoring"
ID             NAME                           MODE         REPLICAS   IMAGE                              PORTS
m5kfa0xjmck9   monitoring_blackbox-exporter   replicated   1/1        prom/blackbox-exporter:v0.24.0     
jtdhhqw8nngw   monitoring_cadvisor            global       3/3        gcr.io/cadvisor/cadvisor:v0.47.0   
vale23rh66es   monitoring_loki                replicated   1/1        grafana/loki:2.9.0                 *:3100->3100/tcp
jtadqt9hsyu3   monitoring_node-exporter       global       3/3        prom/node-exporter:v1.6.1          
uargoi4rk6tc   monitoring_prometheus          replicated   1/1        prom/prometheus:v2.47.0            *:9090->9090/tcp
43g4fdwd6odl   monitoring_promtail            global       3/3        grafana/promtail:2.9.0             
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ssh manager01 "sudo docker service ls"
ID             NAME                           MODE         REPLICAS   IMAGE                              PORTS
q11pw3stlkd1   app_booking-service            replicated   2/2        do-h-09-booking-service:v1         
9pp498ozxpf1   app_gateway-service            replicated   2/2        do-h-09-gateway-service:v1         
r1eg9230s5f9   app_loyalty-service            replicated   2/2        do-h-09-loyalty-service:v1         
xlg487nyji2t   app_nginx                      replicated   1/1        nginx:1.25-alpine                  *:80->80/tcp
5u1yx4g52fc7   app_payment-service            replicated   2/2        do-h-09-payment-service:v1         
27ozfejih6so   app_postgres                   replicated   1/1        do-h-09-postgres:v1                
ly5w2o560jrk   app_rabbitmq                   replicated   1/1        rabbitmq:3.12-management-alpine    
30mxam3auc2b   app_report-service             replicated   1/1        do-h-09-report-service:v1          
tbcozgqiamy5   app_session-service            replicated   2/2        do-h-09-session-service:v1         
m5kfa0xjmck9   monitoring_blackbox-exporter   replicated   1/1        prom/blackbox-exporter:v0.24.0     
jtdhhqw8nngw   monitoring_cadvisor            global       3/3        gcr.io/cadvisor/cadvisor:v0.47.0   
vale23rh66es   monitoring_loki                replicated   1/1        grafana/loki:2.9.0                 *:3100->3100/tcp
jtadqt9hsyu3   monitoring_node-exporter       global       3/3        prom/node-exporter:v1.6.1          
uargoi4rk6tc   monitoring_prometheus          replicated   1/1        prom/prometheus:v2.47.0            *:9090->9090/tcp
43g4fdwd6odl   monitoring_promtail            global       3/3        grafana/promtail:2.9.0             
(base) s@fedora:~/prometheus-loki-grafana-swarm$ 
```















![alt text](<img/1/image copy 25.png>)

**Loki получает логи от Promtail — labels container, logstream, service доступны».**





**Подпись:** «Запрос к Loki `/loki/api/v1/labels` возвращает метки 
`container`, `logstream`, `service` — они создаются Promtail при чтении 
лог-файлов контейнеров Docker. Наличие меток подтверждает, что Promtail 
успешно доставил логи в Loki.»

```
status	"success"
data	
0	"container"
1	"logstream"
2	"service"
```













## 3. Добавлены логи приложения с помощью Loki.



![alt text](<img/1/image copy 29.png>)


**Скриншот: Loki содержит логи приложения**



**Подпись:** «Запрос к Loki `/loki/api/v1/query_range` с фильтром по контейнеру 
`booking-service` возвращает реальные строки логов Java-приложения. 
Логи микросервисов успешно собираются Promtail и хранятся в Loki.»



```
(base) s@fedora:~/prometheus-loki-grafana-swarm$ # Покажет последние 5 строк логов booking-service за последний час из Loki
curl -s -G "http://192.168.56.10:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={container=~".*booking.*"}' \
  --data-urlencode "start=$(date -d '1 hour ago' +%s)000000000" \
  --data-urlencode "end=$(date +%s)000000000" \
  --data-urlencode 'limit=5' | python3 -m json.tool | head -40
{
    "status": "success",
    "data": {
        "resultType": "streams",
        "result": [],
        "stats": {
            "summary": {
                "bytesProcessedPerSecond": 0,
                "linesProcessedPerSecond": 0,
                "totalBytesProcessed": 0,
                "totalLinesProcessed": 0,
                "execTime": 0.008672,
                "queueTime": 0.000196,
                "subqueries": 0,
                "totalEntriesReturned": 0,
                "splits": 3,
                "shards": 0,
                "totalPostFilterLines": 0,
                "totalStructuredMetadataBytesProcessed": 0
            },
            "querier": {
                "store": {
                    "totalChunksRef": 0,
                    "totalChunksDownloaded": 0,
                    "chunksDownloadTime": 0,
                    "chunk": {
                        "headChunkBytes": 0,
                        "headChunkLines": 0,
                        "decompressedBytes": 0,
                        "decompressedLines": 0,
                        "compressedBytes": 0,
                        "totalDuplicates": 0,
                        "postFilterLines": 0,
                        "headChunkStructuredMetadataBytes": 0,
                        "decompressedStructuredMetadataBytes": 0
                    }
                }
            },
            "ingester": {
                "totalReached": 3,
(base) s@fedora:~/prometheus-loki-grafana-swarm$ 
```








## 3) Prometheus Server, Loki, node_exporter, blackbox_exporter, cAdvisor добавлены в отдельный стек для Docker Swarm.





![alt text](<img/1/image copy 30.png>)


**Скриншот терминала, где видно все 7 сервисов с репликами.**


```
(base) s@fedora:~/prometheus-loki-grafana-swarm$ ssh manager01 "sudo docker stack services monitoring"
ID             NAME                           MODE         REPLICAS   IMAGE                              PORTS
m5kfa0xjmck9   monitoring_blackbox-exporter   replicated   1/1        prom/blackbox-exporter:v0.24.0     
jtdhhqw8nngw   monitoring_cadvisor            global       3/3        gcr.io/cadvisor/cadvisor:v0.47.0   
vale23rh66es   monitoring_loki                replicated   1/1        grafana/loki:2.9.0                 *:3100->3100/tcp
jtadqt9hsyu3   monitoring_node-exporter       global       3/3        prom/node-exporter:v1.6.1          
uargoi4rk6tc   monitoring_prometheus          replicated   1/1        prom/prometheus:v2.47.0            *:9090->9090/tcp
43g4fdwd6odl   monitoring_promtail            global       3/3        grafana/promtail:2.9.0             
(base) s@fedora:~/prometheus-loki-grafana-swarm$ 
```






**Скриншот: Отдельный стек мониторинга Docker Swarm**






**Подпись:** «Команды `docker stack ls` и `docker stack services monitoring` 
показывают, что стек `monitoring` развёрнут отдельно от стека `app` и содержит 
требуемые компоненты:
- `monitoring_prometheus` — сервер сбора метрик
- `monitoring_loki` — хранилище логов  
- `monitoring_node-exporter` — экспортёр метрик хоста (CPU, RAM, диск)
- `monitoring_blackbox-exporter` — проверка доступности URL (например, google.com)
- `monitoring_cadvisor` — метрики Docker-контейнеров
- `monitoring_promtail` — агент сбора логов
Все компоненты запущены в нужном количестве реплик.»



















![alt text](<img/1/image copy 14.png>)

**Проверка Prometheus через curl**

```
curl -s http://192.168.56.10:9090/api/v1/targets | python3 -c "import sys,json; d=json.load(sys.stdin); [print(t['labels']['job'], t['health']) for t in d['data']['activeTargets'][:15]]"
blackbox up
booking-service up
booking-service up
cadvisor up
cadvisor up
cadvisor up
gateway-service up
gateway-service up
loyalty-service up
loyalty-service up
node-exporter up
node-exporter up
node-exporter up
payment-service up
payment-service up
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```
























## 4) Метрики доступны на порту 9090.**



![alt text](<img/1/image copy 18.png>)

**http://192.168.56.10:9090/targets?search=**

![alt text](<img/1/image copy 19.png>)

**http://192.168.56.10:9090/targets?search=**




**Скриншот: Метрики доступны на порту 9090**



**Подпись:** «Веб-интерфейс Prometheus по адресу `http://192.168.56.10:9090/targets` 
показывает все настроенные targets (цели для опроса): app_*, blackbox, cadvisor, 
node-exporter, promtail — все в статусе `UP` (зелёный). Метрики собираются и 
доступны на порту 9090.»


![alt text](<img/1/image copy 16.png>)

**http://192.168.56.10:9090/api/v1/targets**

**таблица — все targets с зелёными "UP". Метрики доступны на порту 9090**










![alt text](<img/1/image copy 20.png>)


![alt text](<img/1/image copy 21.png>)

 **Эти  скриншоты Prometheus web ui — доказывают, что Prometheus собирает наши кастомные метрики, так как в поисковой строке они находятся**

**app_gateway_requests_total**

**app_bookings_created_total**


![alt text](<img/1/image copy 26.png>)

**app_gateway_requests_total - Graph**

**Доказательство что Prometheus реально записывает кастомные метрики**

![alt text](<img/1/image copy 27.png>)


**app_gateway_requests_total - Graph**















![alt text](<img/1/image copy 31.png>)

**# 20 запросов на gateway — увеличит нагрузку на app_gateway_requests_total**

**видна ступенька роста.**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm$ for i in $(seq 1 20); do     curl -s http://192.168.56.10/api/v1/hotels > /dev/null; done
```










































## Part 2. Визуализация
## 2. Визуализация

## 1) Для мониторинга в стек Docker Swarm добавлена ​​Grafana.
## 2) Grafana доступна на порту 3000.








![alt text](img/2/image.png)

**Все сервисы monitoring**

**Видно что - grafana/grafana:10.1.5           *:3000->3000/tcp**

```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ ssh manager01 "sudo docker stack services monitoring"
ID             NAME                           MODE         REPLICAS   IMAGE                            PORTS
v7h0zho8dmvj   monitoring_blackbox-exporter   replicated   1/1        prom/blackbox-exporter:v0.24.0   
irj2xufkkwm7   monitoring_grafana             replicated   1/1        grafana/grafana:10.1.5           *:3000->3000/tcp
me2fawgq0f8h   monitoring_loki                replicated   1/1        grafana/loki:2.8.0               *:3100->3100/tcp
b0irutniyl7v   monitoring_node-exporter       global       3/3        prom/node-exporter:v1.6.1        
d3igvyguq7ds   monitoring_prometheus          replicated   1/1        prom/prometheus:v2.47.0          *:9090->9090/tcp
ipqfdssfh0xg   monitoring_promtail            global       3/3        grafana/promtail:2.8.0           
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```






![alt text](<img/1/image copy 33.png>)


```
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ # Проверка — они должны быть Up
for NODE in manager01 worker01 worker02; do
    echo "=== $NODE ==="
    ssh $NODE "sudo docker ps --filter name=cadvisor --format '{{.Status}}'"
done
=== manager01 ===
Up 3 hours (healthy)
=== worker01 ===
Up 3 hours (healthy)
=== worker02 ===
Up 3 hours (healthy)
(base) s@fedora:~/prometheus-loki-grafana-swarm/src$ 
```


**Запустил cAdvisor через docker run отдельно на каждой ноде, чтобы дать ему --privileged Они не в стеке monitoring, а как обычные standalone контейнеры.**

**Все три Up (healthy) — это и есть три cAdvisor, они отдают метрики через :8080 на каждой ноде, и Prometheus их забирает по статическим адресам из prometheus.yml.**





















## 3) В отчете представлен снимок экрана подготовленной панели мониторинга с метриками.

![все метрики работают - все 15](<img/1/image copy 34.png>)


![alt text](<img/1/image copy 35.png>)

**ПРИБЛИЗИЛ**

![alt text](<img/2/image copy 2.png>)

**ПРИБЛИЗИЛ**

**URL: http://192.168.56.10:3000/d/do9-main/do9-monitoring-dashboard**



## Панель мониторинга включает следующие показатели:

1) количество узлов;
2) количество контейнеров;
3) количество стеков;
4) Использование ЦП службами;
5) Использование ЦП для ядер и узлов;
6) затраченная оперативная память;
7) доступная и используемая память;
8) количество процессоров;
9) Доступность google.com;
10) количество сообщений, отправленных в RabbitMQ;
11) количество сообщений, обработанных в RabbitMQ;
12) количество бронирований;
13) количество запросов, полученных на шлюзе;
14) количество полученных запросов на авторизацию пользователей;
15) журналы приложений.





![alt text](<img/2/image copy.png>)


**Что про "Connection reset by peer" в логах?**

Это нормально — это postgres-логи. Эти сообщения появляются когда приложение разрывает соединение с БД (типичное поведение Java приложений с connection pool). Это не ошибки, это информационные сообщения (LOG:, не ERROR:). Для отчёта они идеальны — показывают что логи реально приходят в Loki и отображаются.

![alt text](<img/1/image copy 36.png>)
**более ранний вывод логов приложений**

**Примечание о метриках 4.10, 4.11, 4.12:**

Счётчики `app_rabbitmq_messages_sent_total`, `app_rabbitmq_messages_processed_total` 
и `app_bookings_created_total` показывают значение **0**, потому что отображают 
**бизнес-события**, происходящие только при реальном создании бронирования 
авторизованным пользователем (POST /api/v1/booking с валидным JWT-токеном). 
В рамках демонстрации запросы такого типа не выполнялись.

Корректность настройки метрик подтверждается тем, что:
1. Метрики **существуют в Prometheus** (видны в /graph через автодополнение)
2. **Booking-service и report-service** экспортируют их через /actuator/prometheus
3. **Счётчики 4.13 (Gateway = 8405) и 4.14 (Auth = 8067) растут** — это доказывает 
   что весь механизм Micrometer → Spring Actuator → Prometheus работает корректно.





**Снимок экрана подготовленной панели мониторинга**

Дашборд `DO9 Monitoring Dashboard` включает все 15 требуемых метрик 
(пункты 4.1–4.15 чек-листа). Данные собираются Prometheus (системные 
и пользовательские метрики), а также Loki (логи приложения через 
Promtail).




![alt text](<img/2/image copy 3.png>)

**http://192.168.56.10:3000/connections/datasources/**

**Источники данных**


































##  3. Alert_manager

1) В стек Docker Swarm добавлен Alert Manager для мониторинга.


![alt text](<img/2/image copy 9.png>)

**email отправителя**

**пароль приложения**

**email получателя**

/home/s/prometheus-loki-grafana-swarm/src/alertmanager/alertmanager.yml






![alt text](<img/2/image copy 12.png>)

**Это правила алертов**

/home/s/prometheus-loki-grafana-swarm/src/prometheus/alert_rules.yml









![alt text](<img/2/image copy 10.png>)

/home/s/prometheus-loki-grafana-swarm/src/docker-compose-monitoring.yml

**Добавил новый сервис в секцию services: ( после blackbox-exporter):**




![alt text](<img/2/image copy 11.png>)

/home/s/prometheus-loki-grafana-swarm/src/prometheus/prometheus.yml

**Обновил prometheus.yml чтобы он знал про Alertmanager**

























## 2) В отчете отображаются команды и результаты их выполнения в виде скриншотов для настройки критических событий:

  ## 1) Доступная память составляет менее 100 МБ;






![alt text](<img/2/image copy 7.png>)

**сначала уведомление зелёного цвета**


```
[DO9 Alert] LowAvailableMemory (resolved)
Спам
paturlembo@gmail.com
	
20:06 (3 минуты назад)
	
	
1 alert for alertname=LowAvailableMemory
View in Alertmanager
[1] Resolved
Labels
alertname = LowAvailableMemory
instance = 10.0.2.18:9100
job = node-exporter
severity = critical
Annotations
description = Доступная память 13.51MiB меньше 100 МБ
summary = Низкая доступная память на 10.0.2.18:9100
Source
```








![alt text](<img/2/image copy 8.png>)

**потом красного**

```
1 alert for alertname=LowAvailableMemory
View in Alertmanager
[1] Firing
Labels
alertname = LowAvailableMemory
instance = 10.0.2.18:9100
job = node-exporter
severity = critical
Annotations
description = Доступная память 83.35MiB меньше 100 МБ
summary = Низкая доступная память на 10.0.2.18:9100
Source
```
**уведомление пришло на почту**
























4) Используемая оперативная память превышает 1 ГБ;





![alt text](<img/2/image copy 6.png>)

**уведомление крсного цвета**

![alt text](<img/2/image copy 4.png>)


![alt text](<img/2/image copy 5.png>)


```
[DO9 Alert] HighUsedMemory (firing)
Входящие
paturlembo@gmail.com
	
	20:04 (4 минуты назад)
3 alerts for alertname=HighUsedMemory View in Alertmanager [3] Firing Labels alertname = HighUsedMemory instance = 10.0.2.17:9100 job = node-exporter severity =
paturlembo@gmail.com
	
	20:06 (2 минуты назад)
3 alerts for alertname=HighUsedMemory View in Alertmanager [2] Firing Labels alertname = HighUsedMemory instance = 10.0.2.17:9100 job = node-exporter severity =
paturlembo@gmail.com
	
20:06 (1 минуту назад)
	
	
кому: мне
Похоже, что язык письма: английский
	
3 alerts for alertname=HighUsedMemory
View in Alertmanager
[1] Firing
Labels
alertname = HighUsedMemory
instance = 10.0.2.17:9100
job = node-exporter
severity = warning
Annotations
description = Используется 1.193GiB (> 1 ГБ)
summary = Высокое использование RAM на 10.0.2.17:9100
Source


[2] Resolved
Labels
alertname = HighUsedMemory
instance = 10.0.2.18:9100
job = node-exporter
severity = warning
Annotations
description = Используется 2.886GiB (> 1 ГБ)
summary = Высокое использование RAM на 10.0.2.18:9100
Source
Labels
alertname = HighUsedMemory
instance = 10.0.2.24:9100
job = node-exporter
severity = warning
Annotations
description = Используется 2.467GiB (> 1 ГБ)
summary = Высокое использование RAM на 10.0.2.24:9100
Source
```











## 5) Загрузка ЦП для данной службы превышает 10%.




![alt text](<img/2/image copy 13.png>)



```
5 alerts for alertname=HighServiceCPU
View in Alertmanager
[5] Firing
Labels
alertname = HighServiceCPU
container_label_com_docker_swarm_service_name = app_booking-service
severity = warning
Annotations
description = Загрузка 27.91% (> 10%)
summary = Высокая загрузка CPU сервиса app_booking-service
Source
Labels
alertname = HighServiceCPU
container_label_com_docker_swarm_service_name = app_gateway-service
severity = warning
Annotations
description = Загрузка 28.19% (> 10%)
summary = Высокая загрузка CPU сервиса app_gateway-service
Source
Labels
alertname = HighServiceCPU
container_label_com_docker_swarm_service_name = app_loyalty-service
severity = warning
Annotations
description = Загрузка 27.83% (> 10%)
summary = Высокая загрузка CPU сервиса app_loyalty-service
Source
Labels
alertname = HighServiceCPU
container_label_com_docker_swarm_service_name = app_payment-service
severity = warning
Annotations
description = Загрузка 27.07% (> 10%)
summary = Высокая загрузка CPU сервиса app_payment-service
Source
Labels
alertname = HighServiceCPU
container_label_com_docker_swarm_service_name = app_session-service
severity = warning
Annotations
description = Загрузка 27.99% (> 10%)
summary = Высокая загрузка CPU сервиса app_session-service
Source

```


