
# create java ksvc
kn service create fmt-java --image albertou/fmtok8s-java-function:latest

# config logs for ksvc
kubectl apply -f https://github.com/knative/docs/raw/main/docs/serving/observability/logging/fluent-bit-collector.yaml

# To access the logs through your web browser, enter the command:
kubectl port-forward --namespace logging service/log-collector 9091:80
   
    ** Navigate to http://localhost:9091/