# Part 1 — Метрики ✅ Всё правильно!

Отчёт по метрикам выглядит отлично, можно переходить дальше.

---

## Ответ на твой вопрос: что делают эти команды?

### Разбор команды по частям

```bash
ssh worker01 "sudo docker exec $(sudo docker ps -q --filter name=app_session-service | head -1) wget -qO- http://localhost:8081/actuator/prometheus 2>/dev/null | grep app_"
```

| Часть | Что делает |
|-------|-----------|
| `ssh worker01` | Подключаемся к ноде worker01 |
| `sudo docker ps -q --filter name=app_session-service` | Находим ID контейнера session-service |
| `head -1` | Берём первый контейнер (их может быть 2 реплики) |
| `sudo docker exec ... wget -qO- http://localhost:8081/actuator/prometheus` | **Заходим внутрь контейнера** и делаем HTTP-запрос к Spring Boot Actuator |
| `grep app_` | Фильтруем — показываем только наши кастомные метрики |

### Что такое `/actuator/prometheus`?

Это **эндпоинт**, который Spring Boot + Micrometer **автоматически создают**. Он отдаёт **все метрики** в формате Prometheus — текстовый формат, одна метрика на строку.

### Разбор вывода

```
# HELP app_auth_requests_total Сколько запросов на авторизацию получено
# TYPE app_auth_requests_total counter
app_auth_requests_total{application="session-service",} 3.0
```

| Строка | Значение |
|--------|----------|
| `# HELP ...` | Описание метрики (мы написали в MetricsConfig) |
| `# TYPE ... counter` | Тип — **счётчик** (только растёт, не убывает) |
| `app_auth_requests_total{application="session-service",} 3.0` | **Значение = 3.0** — было 3 запроса на авторизацию с момента старта |

Число `3.0` значит: с момента запуска контейнера session-service получил **3 HTTP-запроса**, которые прошли через наш `AuthRequestCounterFilter`, и каждый раз `counter.increment()` увеличил счётчик на 1.

**Потом Prometheus будет каждые 15 секунд дёргать этот эндпоинт** и сохранять значения в свою базу → Grafana рисует графики.

---
