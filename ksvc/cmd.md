
```shell
export EXTERNAL_IP="127.0.0.1"
export KNATIVE_DOMAIN="$EXTERNAL_IP.sslip.io"
echo KNATIVE_DOMAIN=$KNATIVE_DOMAIN
```

# send CE to broker
```shell
curl -v -X POST http://broker-ingress.knative-eventing.$KNATIVE_DOMAIN/$NAMESPACE/example-broker \
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
kubectl -n $NAMESPACE logs -l serving.knative.dev/service=fmt-java  --tail=100
```

# logs result
```shell
2022-06-28 09:29:55.219  INFO 1 --- [nio-8080-exec-1] o.s.c.f.context.config.RoutingFunction   : Resolved function from provided [routing-expression]  headers['ce-type']
2022-06-28 09:29:55.270  INFO 1 --- [nio-8080-exec-1] uppercase.UpperCaseFunction              : Input CE Id:2
2022-06-28 09:29:55.270  INFO 1 --- [nio-8080-exec-1] uppercase.UpperCaseFunction              : Input CE Spec Version:1.0
2022-06-28 09:29:55.271  INFO 1 --- [nio-8080-exec-1] uppercase.UpperCaseFunction              : Input CE Source:cloud-event-example
2022-06-28 09:29:55.271  INFO 1 --- [nio-8080-exec-1] uppercase.UpperCaseFunction              : Input CE Subject:Uppercase
2022-06-28 09:29:55.271  INFO 1 --- [nio-8080-exec-1] uppercase.UpperCaseFunction              : Input Input{input='salaboy'}
2022-06-28 09:29:55.347  INFO 1 --- [nio-8080-exec-3] o.s.c.f.context.config.RoutingFunction   : Resolved function from provided [routing-expression]  headers['ce-type']
2022-06-28 09:29:55.348  INFO 1 --- [nio-8080-exec-3] uppercase.AppendStringFunction           : Input CE Id:d86facb0-56dc-482f-8ffa-fe35e24fbf75
2022-06-28 09:29:55.348  INFO 1 --- [nio-8080-exec-3] uppercase.AppendStringFunction           : Input CE Spec Version:1.0
2022-06-28 09:29:55.348  INFO 1 --- [nio-8080-exec-3] uppercase.AppendStringFunction           : Input CE Source:http://example.com/uppercase
2022-06-28 09:29:55.348  INFO 1 --- [nio-8080-exec-3] uppercase.AppendStringFunction           : Input CE Subject:Convert to UpperCase
2022-06-28 09:29:55.349  INFO 1 --- [nio-8080-exec-3] uppercase.AppendStringFunction           : Input Input{input='salaboy'}
```