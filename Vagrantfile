Vagrant.configure("2") do |config|
  config.vm.box = "my-ubuntu2204"
  config.ssh.insert_key = false

  config.vm.synced_folder ".", "/home/vagrant/project", type: "rsync",
    rsync__exclude: [".vagrant/", ".git/", "*.box"]

  config.vm.provider :libvirt do |lv|
    lv.driver = "kvm"
    lv.uri = "qemu:///system"
  end

  config.vm.define "manager01" do |node|
    node.vm.hostname = "manager01"
    node.vm.network "private_network",
      ip: "192.168.56.10",
      libvirt__network_name: "do-h-07-net",
      mac: "525400111101"
    node.vm.provider :libvirt do |lv|
      lv.memory = 4096
      lv.cpus = 2
      lv.default_prefix = "do-h-07-"
    end
    node.vm.provision "shell", path: "scripts/install-docker.sh"
    node.vm.provision "shell", path: "scripts/swarm-init.sh"
  end

  config.vm.define "worker01" do |node|
    node.vm.hostname = "worker01"
    node.vm.network "private_network",
      ip: "192.168.56.11",
      libvirt__network_name: "do-h-07-net",
      mac: "525400111102"
    node.vm.provider :libvirt do |lv|
      lv.memory = 3072
      lv.cpus = 2
      lv.default_prefix = "do-h-07-"
    end
    node.vm.provision "shell", path: "scripts/install-docker.sh"
    node.vm.provision "shell", path: "scripts/swarm-join.sh"
  end

  config.vm.define "worker02" do |node|
    node.vm.hostname = "worker02"
    node.vm.network "private_network",
      ip: "192.168.56.12",
      libvirt__network_name: "do-h-07-net",
      mac: "525400111103"
    node.vm.provider :libvirt do |lv|
      lv.memory = 3072
      lv.cpus = 2
      lv.default_prefix = "do-h-07-"
    end
    node.vm.provision "shell", path: "scripts/install-docker.sh"
    node.vm.provision "shell", path: "scripts/swarm-join.sh"
  end
end









# Vagrantfile для развёртывания трёхузлового Docker Swarm кластера

## Построчный разбор с комментариями

```ruby
Vagrant.configure("2") do |config|
  # Главный конфигурационный блок Vagrant. "2" — версия API конфигурации (актуальная)
  
  config.vm.box = "my-ubuntu2204"
  # Базовый box (образ ОС) для всех VM — кастомный Ubuntu 22.04, заранее подготовленный
  
  config.ssh.insert_key = false
  # Отключаем автоматическую генерацию и подмену SSH-ключа Vagrant
  # Используем дефолтный insecure-ключ (для dev-среды — удобнее и быстрее)

  config.vm.synced_folder ".", "/home/vagrant/project", type: "rsync",
    rsync__exclude: [".vagrant/", ".git/", "*.box"]
  # Синхронизация локальной директории с /home/vagrant/project внутри VM через rsync
  # rsync — односторонняя синхронизация (host → guest), быстрее и надёжнее NFS для libvirt
  # Исключаем служебные папки и крупные файлы, которые не нужны в VM

  config.vm.provider :libvirt do |lv|
    # Глобальные настройки провайдера libvirt (используется на Linux вместо VirtualBox)
    lv.driver = "kvm"
    # Драйвер виртуализации KVM — hardware-acceleration для Linux
    lv.uri = "qemu:///system"
    # URI для подключения к локальному QEMU/KVM через systemd
  end

  # ============= MANAGER NODE =============
  config.vm.define "manager01" do |node|
    # Определение первой VM — manager-узла Swarm
    node.vm.hostname = "manager01"
    # Hostname внутри VM
    
    node.vm.network "private_network",
      ip: "192.168.56.10",
      # Статический IP в приватной сети — manager доступен по этому адресу
      libvirt__network_name: "do-h-07-net",
      # Имя виртуальной сети в libvirt — все VM в одной изолированной сети
      mac: "525400111101"
      # Фиксированный MAC-адрес — для предсказуемости и стабильности сетевых настроек
      # Префикс 525400 — стандартный OUI для QEMU/KVM
    
    node.vm.provider :libvirt do |lv|
      lv.memory = 4096
      # 4 ГБ RAM — менеджеру больше ресурсов (Raft consensus, управление кластером)
      lv.cpus = 2
      # 2 виртуальных CPU
      lv.default_prefix = "do-h-07-"
      # Префикс для имён VM в libvirt (избегаем конфликтов имён между проектами)
    end
    
    node.vm.provision "shell", path: "scripts/install-docker.sh"
    # Первый provisioning: устанавливаем Docker
    node.vm.provision "shell", path: "scripts/swarm-init.sh"
    # Второй provisioning: инициализируем Swarm кластер и сохраняем токены в shared folder
  end

  # ============= WORKER 01 =============
  config.vm.define "worker01" do |node|
    # Определение worker-узла №1
    node.vm.hostname = "worker01"
    
    node.vm.network "private_network",
      ip: "192.168.56.11",
      # IP в той же подсети что и manager — могут общаться напрямую
      libvirt__network_name: "do-h-07-net",
      mac: "525400111102"
      # MAC отличается на последнюю цифру
    
    node.vm.provider :libvirt do |lv|
      lv.memory = 3072
      # 3 ГБ — worker'ам меньше памяти, чем менеджеру (нет management overhead)
      lv.cpus = 2
      lv.default_prefix = "do-h-07-"
    end
    
    node.vm.provision "shell", path: "scripts/install-docker.sh"
    # Ставим Docker
    node.vm.provision "shell", path: "scripts/swarm-join.sh"
    # Подключаемся к существующему Swarm-кластеру как worker
    # (берёт токен и IP менеджера из shared folder)
  end

  # ============= WORKER 02 =============
  config.vm.define "worker02" do |node|
    # Worker №2 — полностью аналогичен worker01, отличается только IP и MAC
    node.vm.hostname = "worker02"
    
    node.vm.network "private_network",
      ip: "192.168.56.12",
      libvirt__network_name: "do-h-07-net",
      mac: "525400111103"
    
    node.vm.provider :libvirt do |lv|
      lv.memory = 3072
      lv.cpus = 2
      lv.default_prefix = "do-h-07-"
    end
    
    node.vm.provision "shell", path: "scripts/install-docker.sh"
    node.vm.provision "shell", path: "scripts/swarm-join.sh"
  end
end
```

---

## Краткий монолог для защиты

Этот Vagrantfile описывает полностью автоматическое развёртывание трёхузлового Docker Swarm кластера на локальной машине через libvirt/KVM. Кластер состоит из одного manager-узла с 4 ГБ RAM (повышенный объём для работы Raft-консенсуса и управления кластером) и двух worker-узлов с 3 ГБ RAM каждый — на них развёртываются stateless микросервисы. Все три VM объединены в одну изолированную приватную сеть `do-h-07-net` со статическими IP-адресами (192.168.56.10-12) и фиксированными MAC-адресами для стабильности и предсказуемости. Синхронизация файлов проекта с VM выполняется через rsync — это быстрее и надёжнее NFS в libvirt-окружении, особенно для проектов с множеством мелких файлов. Provisioning каждой VM состоит из двух шагов: установка Docker через `install-docker.sh` и настройка Swarm-роли через `swarm-init.sh` (для менеджера) или `swarm-join.sh` (для worker'ов). Координация между нодами происходит через shared-папку: менеджер сохраняет в неё worker-токен и свой IP, а worker'ы читают эти файлы при провижининге. Префикс `do-h-07-` в именах VM помогает избежать конфликтов с другими проектами на той же хост-машине. Такая конфигурация даёт воспроизводимое окружение для разработки и тестирования Swarm-стеков, идентичное на любой машине с Vagrant и libvirt.

---

## Возможные вопросы на защите и ответы

**1. Что такое Vagrant?**
Это инструмент для автоматизации создания и управления виртуальными машинами. Описываешь VM декларативно в Vagrantfile — Vagrant сам создаёт, настраивает, провижинит. Используется в основном для dev/test окружений.

**2. Чем libvirt отличается от VirtualBox?**
libvirt — это API для управления гипервизорами на Linux, обычно используется с KVM (нативная hardware-виртуализация в ядре Linux). VirtualBox — кроссплатформенный гипервизор от Oracle. libvirt+KVM быстрее на Linux, VirtualBox удобнее для macOS/Windows.

**3. Что такое KVM?**
Kernel-based Virtual Machine — модуль ядра Linux, превращающий его в гипервизор первого типа с использованием аппаратных расширений виртуализации Intel VT-x / AMD-V. Очень производительный.

**4. Что такое QEMU?**
Эмулятор оборудования, который вместе с KVM реализует полноценную виртуализацию. KVM обеспечивает hardware-acceleration, QEMU эмулирует устройства (диски, сеть, видео).

**5. Зачем нужен `config.ssh.insert_key = false`?**
По умолчанию Vagrant генерирует уникальный SSH-ключ для каждой VM и подменяет дефолтный. Это безопаснее, но медленнее. В dev-среде с `false` используется общеизвестный insecure-ключ — удобнее для отладки.

**6. Какие типы synced_folder бывают?**
`virtualbox` (для VirtualBox), `nfs` (NFS-шара, для Linux/macOS), `rsync` (одностороння синхронизация), `smb` (Windows), `9p` (для KVM). Для libvirt rsync — самый простой и надёжный.

**7. Чем rsync хуже NFS?**
rsync — односторонняя синхронизация (host → guest), изменения в VM не попадают на хост. NFS — двусторонний live mount. Но NFS требует настройки сервера на хосте и часто капризничает.

**8. Что значит provider `:libvirt`?**
Указание Vagrant использовать libvirt-провайдер для управления VM. Должен быть установлен плагин `vagrant-libvirt`. Альтернативно — `:virtualbox`, `:vmware_desktop`, `:hyperv`.

**9. Что такое `qemu:///system`?**
URI подключения к QEMU. `system` — глобальный системный демон libvirt (доступ через root/группу libvirt). Альтернатива — `qemu:///session` (пользовательский, без root, но ограниченный).

**10. Почему фиксированные MAC-адреса?**
Чтобы DHCP всегда выдавал один и тот же IP (если используется DHCP), а сетевые правила firewall, мониторинг, конфиги были стабильны между пересозданиями VM. Префикс `525400` — официальный OUI для QEMU.

**11. Что такое приватная сеть в Vagrant?**
Изолированная виртуальная сеть, доступная только между VM и хостом. Внешний мир туда не имеет доступа. Идеально для кластеров — узлы видят друг друга по фиксированным IP.

**12. Зачем менеджеру больше памяти, чем worker'ам?**
Менеджер ведёт Raft-журнал кластера, хранит состояние всех сервисов, обрабатывает API-запросы, делает scheduling. Это требует ресурсов. Worker'ы просто запускают контейнеры.

**13. Что произойдёт, если упадёт единственный manager?**
Кластер потеряет управление: нельзя будет деплоить, скейлить, обновлять сервисы. Уже запущенные контейнеры продолжат работать, но self-healing и orchestration пропадут. В production нужно минимум 3 менеджера.

**14. Почему 3 менеджера, а не 2?**
Raft требует кворума (большинства). Для 2 узлов кворум = 2 (не переживёт падение одного). Для 3 — кворум 2 (переживёт падение одного). Для 5 — кворум 3 (переживёт двух).

**15. Что делает `node.vm.provision "shell"`?**
Запускает указанный shell-скрипт внутри VM от root. Provisioning'и выполняются последовательно в порядке объявления. Если изменился скрипт — можно перезапустить через `vagrant provision`.

**16. Зачем `default_prefix`?**
Без префикса libvirt создаст VM с именами вроде "manager01", "worker01". Если на хосте есть другой проект с такими же именами — конфликт. Префикс делает имена уникальными: "do-h-07-manager01".

**17. Как worker узнаёт о существующем кластере?**
Через shared folder: manager при init сохраняет worker-токен и свой IP в файлы в общую папку. Worker при провижининге читает эти файлы и выполняет `docker swarm join`. Это синхронизация через файловую систему.

**18. Что произойдёт при `vagrant up`?**
Vagrant создаст все VM последовательно: для каждой создаёт виртуальный диск (из box), настраивает сеть, запускает, синхронизирует папку, выполняет провижининг. По умолчанию — параллельно, но зависимости можно настроить.

**19. Можно ли запустить только одну VM?**
Да: `vagrant up manager01`. Удобно для частичной отладки. Но в нашем случае worker'ы зависят от menager (нужен токен), поэтому правильный порядок: сначала manager, потом workers.

**20. Что такое box в Vagrant?**
Готовый образ VM (диск + метаданные). Можно скачать с Vagrant Cloud или собрать самому (например, через Packer). `my-ubuntu2204` — кастомный box, видимо, заранее подготовленный с базовыми пакетами.

**21. Сколько ресурсов потребляет весь стенд?**
RAM: 4 + 3 + 3 = 10 ГБ. CPU: 2 + 2 + 2 = 6 vCPU. Плюс диски. Для разработки нужна машина минимум с 16 ГБ RAM и 8 ядрами.

**22. Как удалить весь стенд?**
`vagrant destroy -f` — удаляет все VM проекта. `-f` без подтверждения. Диски и сети чистятся автоматически.

**23. Почему IP именно 192.168.56.x?**
Это типовая подсеть для приватных сетей Vagrant (исторически дефолт VirtualBox). В нашем случае настраивается через libvirt-сеть `do-h-07-net`. Любая RFC1918 подсеть подойдёт.

**24. Что будет при изменении Vagrantfile и `vagrant up`?**
Зависит от типа изменения: сеть, ресурсы — потребуется `vagrant reload`. Провижининг — `vagrant provision`. Box — `vagrant destroy && vagrant up` (полное пересоздание).

**25. В чём преимущество такого подхода перед ручной настройкой?**
Воспроизводимость, версионирование (Vagrantfile в git), переносимость (работает на любой машине с Vagrant), быстрое восстановление при сломанной VM. Это Infrastructure as Code в миниатюре.