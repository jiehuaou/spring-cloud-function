# Function project

Welcome to your new Function project!

This sample project contains a single function based on Spring Cloud Function: `functions.CloudFunctionApplication.uppercase()`, which returns the uppercase of the data passed via CloudEvents.

## Overview

This Project is running as a service deployed inside Knative environment.

![knative info](./overview.jpg "knative-diagram")

## Local execution

Make sure that `Java 11 SDK` is installed.

To start server locally run `./mvnw spring-boot:run`.
The command starts http server and automatically watches for changes of source code.
If source code changes the change will be propagated to running server. It also opens debugging port `5005`
so a debugger can be attached if needed.

To run tests locally run `./mvnw test`.

## The `func` CLI

It's recommended to set `FUNC_REGISTRY` environment variable.

```shell script
# replace ~/.bashrc by your shell rc file
# replace docker.io/johndoe with your registry
export FUNC_REGISTRY=docker.io/johndoe
echo "export FUNC_REGISTRY=docker.io/johndoe" >> ~/.bashrc
```

### Building

This command builds an OCI image for the function. By default, this will build a GraalVM native image.

```shell script
func build -v                  # build native image
```

**Note**: If you want to disable the native build, you need to edit the `func.yaml` file and
remove (or set to false) the following BuilderEnv variable:
```
buildEnvs:
  - name: BP_NATIVE_IMAGE
    value: "true"
```


### Running

This command runs the func locally in a container
using the image created above.

```shell script
func run
```

### Deploying

This command will build and deploy the function into cluster.

```shell script
func deploy -v # also triggers build
```

## Function Return

```java
@Component("UppercaseRequestedEvent")
public class UpperCaseFunction implements Function<Message<Input>, Message<Output>> {
    private static final Logger LOGGER = Logger.getLogger(
      UpperCaseFunction.class.getName());

  @Override
  public Message<Output> apply(Message<Input> inputMessage) {
    HttpHeaders httpHeaders = HeaderUtils.fromMessage(inputMessage.getHeaders());

      Input input = inputMessage.getPayload();
      LOGGER.log(Level.INFO, "Input {0} ", input);
      Output output = new Output();
      output.setInput(input.getInput());
      output.setOperation(httpHeaders.getFirst(SUBJECT));
      output.setOutput(input.getInput() != null ? input.getInput().toUpperCase() : "NO DATA");
      return CloudEventMessageBuilder.withData(output)
        .setType("UpperCasedEvent").setId(UUID.randomUUID().toString())
        .setSubject("Converted to UpperCase")
        .setSource(URI.create("http://example.com/uppercase")).build();
  }
}
```

if function returns CloudEvent, this event will be stored into Broker for further process.

if function returns Non-CloudEvent, such as string, this text will be simply ignored by Knative.

if function throw an exception, Knative will retry the call based on pre-config error-retry policy, until the message was put into DLQ finally. 


## Function Routing Logic

Spring Cloud Functions allows you to route CloudEvents to specific functions using the `Ce-Type` attribute.
For this example, the CloudEvent is routed to the `UppercaseRequestedEvent` function. You can define multiple functions inside this project
and then use the `Ce-Type` attribute to route different CloudEvents to different Functions.
Check the `application.properties` file for the `functionRouter` configurations.

application.properties
```
spring.cloud.function.definition=functionRouter
spring.cloud.function.routing-expression=headers["ce-type"]
```

CloudEvents Sample to be sent
```json
{
  "Ce-Type" : "UppercaseRequestedEvent",
  "Ce-Id" : "1234" ,
  "data" : "any"
}
```

Knative Trigger
```
apiVersion: eventing.knative.dev/v1
kind: Trigger
metadata:
  name: uppercase-java-function-trigger
  namespace: default
spec:
  broker: example-broker
  filter:
    attributes:
      type: UppercaseRequestedEvent
  subscriber:
    ref:
      apiVersion: serving.knative.dev/v1
      kind: Service
      name: fmt-java
```

Java Bean with Component Name `UppercaseRequestedEvent`
```java
@Component("UppercaseRequestedEvent")
public class UpperCaseFunction implements  Function <Message<Input>, Message<Output>> 
{ ...}
```

then this Func will be call with the CloudEvents with type `UppercaseRequestedEvent`.

---

Notice that you can also use `path-based` routing and send the any event type by specifying the function path,
for this example: "$URL/UppercaseRequestedEvent".

For the examples below, please be sure to set the `URL` variable to the route of your function.

You get the route by following command.

```shell script
func info
```

Note the value of **Routes:** from the output, set `$URL` to its value.

__TIP__:

If you use `kn` then you can set the url by:

```shell script
# kn service describe <function name> and show route url
export URL=$(kn service describe $(basename $PWD) -ourl)
```

### cURL

Using CloudEvents `Ce-Type` routing:
```shell script
curl -v "$URL/" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Subject:UppercaseRequestedEvent" \
  -H "Ce-Source:cloud-event-example" \
  -H "Ce-Type:UppercaseRequestedEvent" \
  -H "Ce-Specversion:1.0" \
  -d "{\"input\": \"$(whoami)\"}\""
```

Using Path-Based routing:
```shell script
curl -v "$URL/UppercaseRequestedEvent" \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Subject:UppercaseRequestedEvent" \
  -H "Ce-Source:cloud-event-example" \
  -H "Ce-Type:UppercaseRequestedEvent" \
  -H "Ce-Specversion:1.0" \
  -d "{\"input\": \"$(whoami)\"}\""
```

### HTTPie

Using CloudEvents `Ce-Type` routing:
```shell script
http -v "$URL/" \
  Content-Type:application/json \
  Ce-Id:1 \
  Ce-Subject:UppercaseRequestedEvent \
  Ce-Source:cloud-event-example \
  Ce-Type:UppercaseRequestedEvent \
  Ce-Specversion:1.0 \
  input=$(whoami)
```

Using Path-Based routing:
```shell script
http -v "$URL/UppercaseRequestedEvent" \
  Content-Type:application/json \
  Ce-Id:1 \
  Ce-Subject:UppercaseRequestedEvent \
  Ce-Source:cloud-event-example \
  Ce-Type:UppercaseRequestedEvent \
  Ce-Specversion:1.0 \
  input=$(whoami)
```

## Cleanup

To remove the deployed function from your cluster, run:

```shell
func delete
```
