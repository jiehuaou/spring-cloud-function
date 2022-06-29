
```shell
export EXTERNAL_IP="127.0.0.1"
export KNATIVE_DOMAIN="127.0.0.1.sslip.io"
export NAMESPACE="default"
echo KNATIVE_DOMAIN=$KNATIVE_DOMAIN
```

```shell

kubectl apply -f broker.yaml
kubectl apply -f broker-dns.yaml

kubectl apply -f func-java.yaml
kubectl apply -f trigger-java.yaml

```

# Send CE to java
```shell
curl -s -v http://fmt-java.default.127.0.0.1.sslip.io \
-X POST \
-H "Content-Type:application/json" \
-H "Ce-Id:1" \
-H "Ce-Subject:Uppercase" \
-H "Ce-Source:cloud-event-example" \
-H "Ce-Type:UppercaseRequestedEvent" \
-H "Ce-Specversion:1.0" \
-d "{\"input\": \"salaboy\"}"

kubectl -n "$NAMESPACE" logs -l serving.knative.dev/service=fmt-java  --tail=100
```

```shell
2022-06-29 06:43:56.423  INFO 1 --- [nio-8080-exec-3] o.s.c.f.context.config.RoutingFunction   : Resolved function from provided [routing-expression]  headers['ce-type']
2022-06-29 06:43:56.466  INFO 1 --- [nio-8080-exec-3] uppercase.UpperCaseFunction              : Input CE Id:1
2022-06-29 06:43:56.466  INFO 1 --- [nio-8080-exec-3] uppercase.UpperCaseFunction              : Input CE Spec Version:1.0
2022-06-29 06:43:56.466  INFO 1 --- [nio-8080-exec-3] uppercase.UpperCaseFunction              : Input CE Source:cloud-event-example
2022-06-29 06:43:56.467  INFO 1 --- [nio-8080-exec-3] uppercase.UpperCaseFunction              : Input CE Subject:Uppercase
2022-06-29 06:43:56.467  INFO 1 --- [nio-8080-exec-3] uppercase.UpperCaseFunction              : Input Input{input='salaboy', output='null'}

```

# Send CE to broker, 
will trigger 3 invoking {  UpperCaseFunction => AppendStringFunction => DisplayFunction }
```shell
curl -s -v  "http://broker-ingress.knative-eventing.$KNATIVE_DOMAIN/$NAMESPACE/example-broker" \
    -X POST \
    -H "Content-Type:application/json" \
    -H "Ce-Id:2" \
    -H "Ce-Subject:Uppercase" \
    -H "Ce-Source:cloud-event-example" \
    -H "Ce-Type:UppercaseRequestedEvent" \
    -H "Ce-Specversion:1.0" \
    -d "{\"input\": \"salaboy\"}"
```

# check logs on service
```shell
kubectl -n "$NAMESPACE" logs -l serving.knative.dev/service=fmt-java  --tail=100
```

# logs result
```shell
2022-06-29 06:44:23.560  INFO 1 --- [nio-8080-exec-7] o.s.c.f.context.config.RoutingFunction   : Resolved function from provided [routing-expression]  headers['ce-type']
2022-06-29 06:44:23.561  INFO 1 --- [nio-8080-exec-7] uppercase.UpperCaseFunction              : Input CE Id:2
2022-06-29 06:44:23.561  INFO 1 --- [nio-8080-exec-7] uppercase.UpperCaseFunction              : Input CE Spec Version:1.0
2022-06-29 06:44:23.561  INFO 1 --- [nio-8080-exec-7] uppercase.UpperCaseFunction              : Input CE Source:cloud-event-example
2022-06-29 06:44:23.561  INFO 1 --- [nio-8080-exec-7] uppercase.UpperCaseFunction              : Input CE Subject:Uppercase
2022-06-29 06:44:23.561  INFO 1 --- [nio-8080-exec-7] uppercase.UpperCaseFunction              : Input Input{input='salaboy', output='null'}
2022-06-29 06:44:23.575  INFO 1 --- [nio-8080-exec-8] o.s.c.f.context.config.RoutingFunction   : Resolved function from provided [routing-expression]  headers['ce-type']
2022-06-29 06:44:23.577  INFO 1 --- [nio-8080-exec-8] uppercase.AppendStringFunction           : Input CE Id:674f58fb-68e2-4ceb-9078-ae9b3a3eb784
2022-06-29 06:44:23.577  INFO 1 --- [nio-8080-exec-8] uppercase.AppendStringFunction           : Input CE Spec Version:1.0
2022-06-29 06:44:23.577  INFO 1 --- [nio-8080-exec-8] uppercase.AppendStringFunction           : Input CE Source:http://example.com/uppercase
2022-06-29 06:44:23.577  INFO 1 --- [nio-8080-exec-8] uppercase.AppendStringFunction           : Input CE Subject:Convert to UpperCase
2022-06-29 06:44:23.577  INFO 1 --- [nio-8080-exec-8] uppercase.AppendStringFunction           : Input Input{input='salaboy', output='SALABOY'}
2022-06-29 06:44:23.587  INFO 1 --- [nio-8080-exec-9] o.s.c.f.context.config.RoutingFunction   : Resolved function from provided [routing-expression]  headers['ce-type']
2022-06-29 06:44:23.588  INFO 1 --- [nio-8080-exec-9] uppercase.DisplayFunction                : Input CE Id:028b9b70-9840-41ab-a1e9-64ca68efca80
2022-06-29 06:44:23.588  INFO 1 --- [nio-8080-exec-9] uppercase.DisplayFunction                : Input CE Spec Version:1.0
2022-06-29 06:44:23.588  INFO 1 --- [nio-8080-exec-9] uppercase.DisplayFunction                : Input CE Source:http://example.com/append-extra
2022-06-29 06:44:23.588  INFO 1 --- [nio-8080-exec-9] uppercase.DisplayFunction                : Input CE Subject:append extra string
2022-06-29 06:44:23.588  INFO 1 --- [nio-8080-exec-9] uppercase.DisplayFunction                : Input Input{input='salaboy', output='salaboy-extra-string'}
```