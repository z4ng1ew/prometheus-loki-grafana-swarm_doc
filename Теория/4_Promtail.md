Переходим к Part 1.3 — Логи через Loki + Promtail
Что такое Promtail?

Promtail — агент, который:

     Читает логи контейнеров Docker (из /var/lib/docker/containers/...)
     Добавляет лейблы (имя сервиса, нода)
     Отправляет в Loki

Loki — хранилище логов (как Prometheus, но для логов).