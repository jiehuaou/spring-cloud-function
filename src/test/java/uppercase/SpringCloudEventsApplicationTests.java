package uppercase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.ID;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SOURCE;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SPECVERSION;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SUBJECT;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = SpringCloudEventsApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class SpringCloudEventsApplicationTests {
  
  @Autowired
  private TestRestTemplate rest;

  @Autowired
  ObjectMapper objectMapper = new ObjectMapper();

  /**
   * test to route event to UppercaseRequestedEvent Function, via path '/UppercaseRequestedEvent'
   */
  @Test
  public void testUpperCaseJsonInput() throws Exception {

    Input input = new Input();

    input.setInput("hello");

    HttpHeaders ceHeaders = new HttpHeaders();
    ceHeaders.add(SPECVERSION, "1.0");
    ceHeaders.add(ID, UUID.randomUUID()
        .toString());
    ceHeaders.add(TYPE, EventType.UPPER_CASE_REQUEST_EVENT);
    ceHeaders.add(SOURCE, "http://localhost:8080/");
    ceHeaders.add(SUBJECT, "Convert to UpperCase");

    ResponseEntity<String> response = this.rest.exchange(
        RequestEntity.post(new URI("/" + EventType.UPPER_CASE_REQUEST_EVENT))
            .contentType(MediaType.APPLICATION_JSON)
            .headers(ceHeaders)
            .body(input),
        String.class);

    assertThat(response.getStatusCode()
        .value(), equalTo(200));
    String body = response.getBody();
    assertThat(body, notNullValue());
    Output output = objectMapper.readValue(body,
        Output.class);
    assertThat(output, notNullValue());
    assertThat(output.getInput(), equalTo("hello"));
    assertThat(output.getOperation(), equalTo("Convert to UpperCase"));
    assertThat(output.getOutput(), equalTo("HELLO"));
    assertThat(output.getError(), nullValue());
  }

  /**
   * test to route event to UppercaseRequestedEvent Function, with ce-type['UppercaseRequestedEvent']
   */
  @Test
  public void testUpperCaseRoutingBasedOnType() throws Exception {

    Input input = new Input();

    input.setInput("hello");

    HttpHeaders ceHeaders = new HttpHeaders();
    ceHeaders.add(SPECVERSION, "1.0");
    ceHeaders.add(ID, UUID.randomUUID()
      .toString());
    ceHeaders.add(TYPE, EventType.UPPER_CASE_REQUEST_EVENT);
    ceHeaders.add(SOURCE, "http://localhost:8080/");
    ceHeaders.add(SUBJECT, "Convert to UpperCase");

    ResponseEntity<String> response = this.rest.exchange(
      RequestEntity.post(new URI("/"))
        .contentType(MediaType.APPLICATION_JSON)
        .headers(ceHeaders)
        .body(input),
      String.class);

    assertThat(response.getStatusCode()
      .value(), equalTo(200));
    String body = response.getBody();
    assertThat(body, notNullValue());
    Output output = objectMapper.readValue(body,
      Output.class);
    assertThat(output, notNullValue());
    assertThat(output.getInput(), equalTo("hello"));
    assertThat(output.getOperation(), equalTo("Convert to UpperCase"));
    assertThat(output.getOutput(), equalTo("HELLO"));
    assertThat(output.getError(), nullValue());
  }

  /**
   * test to route event to AppendStringFunction Function, with ce-type[UPPER_CASE_DONE_EVENT]
   */
  @Test
  public void testAppendStringOnType() throws Exception {

    Input input = new Input();

    input.setInput("HELLO");

    HttpHeaders ceHeaders = new HttpHeaders();
    ceHeaders.add(SPECVERSION, "1.0");
    ceHeaders.add(ID, UUID.randomUUID().toString());
    ceHeaders.add(TYPE, EventType.UPPER_CASE_DONE_EVENT);
    ceHeaders.add(SOURCE, "http://localhost:8080/");

    ResponseEntity<String> response = this.rest.exchange(
            RequestEntity.post(new URI("/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(ceHeaders)
                    .body(input),
            String.class);

    assertThat(response.getStatusCode().value(), equalTo(200));
    String body = response.getBody();
    assertThat(body, notNullValue());
    Output output = objectMapper.readValue(body, Output.class);
    assertThat(output, notNullValue());
    assertThat(output.getInput(), equalTo("HELLO"));
//    assertThat(output.getOperation(), equalTo("Appended extra string"));
    assertThat(output.getOutput(), equalTo("HELLO-world"));
    assertThat(output.getError(), nullValue());
    assertThat(response.getHeaders().getFirst("Ce-Type"), equalTo(EventType.ALL_DONE_EVENT));

  }

  /**
   * test to route event to DisplayFunction Function, with ce-type[ALL_DONE_EVENT]
   */
  @Test
  public void testDisplayFunctionOnType() throws Exception {

    Input input = new Input();

    input.setInput("HELLO-world");

    HttpHeaders ceHeaders = new HttpHeaders();
    ceHeaders.add(SPECVERSION, "1.0");
    ceHeaders.add(ID, UUID.randomUUID().toString());
    ceHeaders.add(TYPE, EventType.ALL_DONE_EVENT);
    ceHeaders.add(SOURCE, "http://localhost:8080/");

    ResponseEntity<String> response = this.rest.exchange(
            RequestEntity.post(new URI("/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(ceHeaders)
                    .body(input),
            String.class);

    assertThat(response.getStatusCode().value(), equalTo(200));
    String body = response.getBody();
    assertThat(body, equalTo("ok"));

  }

}
