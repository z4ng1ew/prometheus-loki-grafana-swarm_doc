# Хорошие новости и плохие новости

## ✅ Хорошие новости

- **manager01** — живой, пингуется, Docker есть, Swarm Leader (`sudo docker node ls` показывает его). Отлично.
- **worker02** — пингуется (`192.168.56.12`), Docker установлен (видно в логах `Docker version 29.4.3`). Просто **не зашёл в Swarm**.
- **Маневрировать осталось чуть-чуть** — все три виртуалки физически работают.

## ❌ Плохие новости — два дефекта

### Дефект 1. worker01 потерян по сети

```
PING 192.168.56.11 ...
From 192.168.56.1 icmp_seq=1 Destination Host Unreachable
```

`Destination Host Unreachable` означает — приватная сеть знает, что 192.168.56.0/24 идёт через мост, но **внутри** виртуалки `worker01` интерфейс `ens6` или не поднят, или взял другой IP. ВМ работает (`virsh list` показывает её running), но в сети 192.168.56.0/24 её нет.

### Дефект 2. swarm-join.sh принципиально не может работать

Смотри вывод worker02:
```
worker02: [swarm-join] ОШИБКА: не найдены файлы 
    /home/vagrant/project/swarm/worker-token.txt или 
    /home/vagrant/project/swarm/manager-ip.txt
```

Скрипт ищет файлы, которые `swarm-init.sh` создал **только на manager01**. У тебя в Vagrantfile стоит `type: "rsync"` для synced_folder — это **одностороннее** копирование с хоста на ВМ. Manager01 пишет файл — он остаётся на manager01, на worker'ы его не передают. Значит этот скрипт **никогда** не отработал бы как задумано (если ты в DO_07 запускал `vagrant ssh` и руками копировал — это была твоя ручная работа).

---

## Решение — простое и быстрое

Мы **не будем чинить** скрипт `swarm-join.sh`. Вместо этого:

1. Пересоздадим worker01 (он сломан по сети).
2. Заджойним **оба worker'а в Swarm руками** — это 2 команды через SSH с хоста, которые ты уже видел в моём прошлом сообщении (раздел Шаг 4).

---

## Команды по порядку

### Шаг A — Пересоздать worker01

```bash
cd ~/prometheus-loki-grafana-swarm
vagrant destroy worker01 -f
sleep 10
vagrant up worker01
```

Дождись когда установка Docker закончится. В конце скрипт swarm-join снова напишет «ОШИБКА: не найдены файлы» — **это не страшно**, мы заджойним вручную в следующем шаге.

### Шаг B — Проверить, что worker01 теперь пингуется

```bash
ping -c 2 192.168.56.11
ping -c 2 192.168.56.12

# И что SSH работает
ssh worker01 "hostname && which docker"
ssh worker02 "hostname && which docker"
```

**Ожидаемо:** оба должны пинговаться и в каждом `hostname` совпадает, `docker` найден в `/usr/bin/docker`.

⚠️ Если `ping 192.168.56.11` снова даст «Destination Host Unreachable» — напиши мне, придётся лезть в virsh console и руками поднимать интерфейс через netplan. Скорее всего пересоздание поможет.

### Шаг C — Вручную заджойнить оба worker'а в Swarm

```bash
# Свежий токен с manager01
TOKEN=$(ssh manager01 "sudo docker swarm join-token -q worker")
echo "Token: $TOKEN"

# Если worker уже в Swarm — leave (пригодится для worker02, который мог в неудачной попытке частично присоединиться)
ssh worker01 "sudo docker swarm leave --force 2>/dev/null; true"
ssh worker02 "sudo docker swarm leave --force 2>/dev/null; true"

# Джойним
ssh worker01 "sudo docker swarm join --token $TOKEN 192.168.56.10:2377"
ssh worker02 "sudo docker swarm join --token $TOKEN 192.168.56.10:2377"

# Проверка — должно быть 3 ноды, все Ready
ssh manager01 "sudo docker node ls"
```

Ожидаемый вывод:
```
ID            HOSTNAME    STATUS    AVAILABILITY   MANAGER STATUS   ENGINE VERSION
fflgfig5...   manager01   Ready     Active         Leader           29.4.3
xxxxxxxxx     worker01    Ready     Active                          29.4.3
yyyyyyyyy     worker02    Ready     Active                          29.4.3
```

---

## Сделай эти три блока и пришли вывод

Когда все три ноды будут в статусе `Ready`, мы **сразу** переходим к Этапу 3 — добавлению Micrometer в Java-код. Кластер будет готов к деплою.

📋 **Для отчёта:** сделай скриншот вывода `ssh manager01 "sudo docker node ls"` — это **закрывает чек-лист 1.1** (`Vagrantfile создаёт manager, node01, node02`) и подтверждает «*Docker Swarm из первого проекта*».












# Почти идеально — осталось разобраться с worker02

## Что у нас сейчас

| Узел | Пингуется | SSH работает | Docker есть | В Swarm |
|---|---|---|---|---|
| manager01 (192.168.56.10) | ✅ | ✅ | ✅ | ✅ Leader |
| worker01 (192.168.56.11) | ✅ | ✅ | ✅ | ❌ |
| worker02 (192.168.56.12) | ❌ | ✅ ⚠️ | ✅ | ❌ |

Странная картина: `ping 192.168.56.12` — 100% loss, но `ssh worker02 "hostname"` отвечает `worker02` и видит `/usr/bin/docker`. Парадокс?

**Объяснение:** SSH ходит через **default-сеть** Vagrant (`192.168.121.x`, она через NAT), а ping ты пускаешь в **приватную сеть** (`192.168.56.x`). Они независимы. Хост видит worker02 через 121.x, но **manager01 → worker02** через приватную сеть **прямо сейчас не пройдёт** — а это именно то, что нужно для Swarm.

Скорее всего worker02 опять «потерял» интерфейс `ens6` после `vagrant up worker01` (libvirt мог перетряхнуть сеть). Сейчас починим.

---

## Шаг A — Заджойнить worker01 (это сразу сделаем)

worker01 пингуется, можно сразу джойнить:

```bash
TOKEN=$(ssh manager01 "sudo docker swarm join-token -q worker")
echo "Token: $TOKEN"

# На всякий случай leave (если был частично присоединён)
ssh worker01 "sudo docker swarm leave --force 2>/dev/null; true"

# Джойним
ssh worker01 "sudo docker swarm join --token $TOKEN 192.168.56.10:2377"

# Проверка — должно быть 2 ноды Ready
ssh manager01 "sudo docker node ls"
```

---

## Шаг B — Диагностика worker02

```bash
# Что внутри worker02 с интерфейсами и IP
ssh worker02 "ip -br addr"

# Посмотри в выводе строчку про ens6 — должен быть 192.168.56.12/24
# Если её нет или статус DOWN — будем чинить
```

Покажи мне вывод этой команды. Возможны три варианта:

**Вариант 1** — `ens6` есть и со статусом `UP`, IP `192.168.56.12/24` на месте. Тогда проблема в маршрутизации со стороны хоста или в ARP-таблице. Чиним отдельно.

**Вариант 2** — `ens6` есть, но `DOWN` или без IP. Поднимаем интерфейс одной командой.

**Вариант 3** — `ens6` нет совсем. Виртуалка получила только default-интерфейс. Нужно перезагрузить ВМ.

---

## Шаг C — Быстрый фикс на worker02 (попробуй сразу)

Скорее всего поможет простой ребут гостевой ОС:

```bash
# Перезагрузка worker02 изнутри
ssh worker02 "sudo reboot" || true

sleep 60

# Проверка
ping -c 2 192.168.56.12
```

Если ping пошёл — отлично, тогда:

```bash
ssh worker02 "sudo docker swarm leave --force 2>/dev/null; true"
ssh worker02 "sudo docker swarm join --token $TOKEN 192.168.56.10:2377"

# Финал — должно быть 3 ноды Ready
ssh manager01 "sudo docker node ls"
```

---

## Если ребут не помог

Тогда сделаем точечный фикс — поднимем приватный интерфейс изнутри:

```bash
# Если ens6 в DOWN или без IP:
ssh worker02 "sudo ip link set ens6 up; sudo ip addr add 192.168.56.12/24 dev ens6 2>/dev/null; ip -br addr | grep 192.168.56"

# Проверка
ping -c 2 192.168.56.12
```

---

## Что мне прислать

1. Вывод команд из **Шага A** (после джойна worker01)
2. Вывод `ssh worker02 "ip -br addr"` из **Шага B**
3. Вывод **Шага C** (если ребут помог — пинг и финальный `docker node ls`)

После того как все три ноды будут `Ready` — **сразу** переходим к Этапу 3 (Micrometer в коде). Дальше работа уже не зависит от Vagrant — всё делаем на хосте, в виртуалки только переносим готовые образы.

📋 **Маленькое замечание для отчёта:** проблемы с автоматическим join'ом — это **известный косяк твоего scripts/swarm-join.sh** (он требует общий volume между ВМ, а у тебя rsync — односторонний). В DO_07 ты этого не заметил, потому что, вероятно, копировал файлы руками. Когда дойдёшь до отчёта, можешь упомянуть, что **присоединение worker'ов выполнялось вручную через `docker swarm join` после провижнинга** — это валидная практика, и чек-лист 1.2 («нет shell-скриптов установки Docker») это не нарушает.