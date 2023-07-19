package uppercase;

import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.cloud.function.web.util.HeaderUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.*;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SUBJECT;
import static uppercase.EventType.*;

/**
 * 1) handle type=UpperCasedEvent event
 * 2) return type=DoneEvent to broker then process by DisplayFunction
 */
@Component(UPPER_CASE_DONE_EVENT)
public class AppendStringFunction implements Function<Message<Input>, Message<Output>> {
    private static final Logger LOGGER = Logger.getLogger(AppendStringFunction.class.getName());

    @Override
    public Message<Output> apply(Message<Input> inputMessage) {
        HttpHeaders httpHeaders = HeaderUtils.fromMessage(inputMessage.getHeaders());

        LOGGER.log(Level.INFO, "Input CE Id:{0}", httpHeaders.getFirst(ID));
        LOGGER.log(Level.INFO, "Input CE Spec Version:{0}", httpHeaders.getFirst(SPECVERSION));
        LOGGER.log(Level.INFO, "Input CE Source:{0}", httpHeaders.getFirst(SOURCE));
        LOGGER.log(Level.INFO, "Input CE Subject:{0}", httpHeaders.getFirst(SUBJECT));

        Input input = inputMessage.getPayload();
        LOGGER.log(Level.INFO, "Input {0} ", input);
        Output output = new Output();
        output.setInput(input.getInput());
        output.setOperation(httpHeaders.getFirst(SUBJECT));
        output.setOutput(input.getInput() != null ? input.getInput() + "-world" : "NO DATA");
        return CloudEventMessageBuilder.withData(output)
                .setType(ALL_DONE_EVENT)
                .setId(UUID.randomUUID().toString())
                .setSubject("Appended extra string")
                .setSource(URI.create("http://example.com/append-extra")).build();
    }
}
