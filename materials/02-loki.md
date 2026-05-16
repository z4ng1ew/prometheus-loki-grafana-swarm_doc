# Loki

Inspired by Prometheus, Loki is a horizontally scalable, highly available, multi-user log aggregation system. It is designed to be cost-effective and user-friendly.

Like Prometheus, Loki has a client-server architecture. Loki's kernel stores the logs of the entire system, and numerous agents — local metrics collectors (promtail) — pass the logs to Loki.

To retrieve logs from Loki, a query in LogQL, a language similar to PromQL, is required.

A standard LogQL query consists of two parts: a selector and a filter. The selector searches for labels that index logs in Loki, and the filter selects a subset of the logs identified by the selector using a search query or regular expression.

For example:

```
{cluster="ops-tools1",container="ingress-nginx"}
    | json
    | __error__ != "JSONParserErr"
```
where
`{cluster="ops-tools1",container="ingress-nginx"}` is a selector, and `| json` and `| __error__ != "JSONParserErr"` are filters.
