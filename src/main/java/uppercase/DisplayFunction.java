package uppercase;

import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.cloud.function.web.util.HeaderUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.*;
import static org.springframework.cloud.function.cloudevent.CloudEventMessageUtils.SUBJECT;
import static uppercase.EventType.ALL_DONE_EVENT;

/**
 * does not return Message<T> ( CloudEvent ), 
 * which will not be processed by Broker, meaning the event flow ending here.
 */
@Component(ALL_DONE_EVENT)
public class DisplayFunction implements Function<Message<Input>, String> {
    private static final Logger LOGGER = Logger.getLogger(DisplayFunction.class.getName());

    public void acceptImpl(Message<Input> inputMessage) {
        HttpHeaders httpHeaders = HeaderUtils.fromMessage(inputMessage.getHeaders());

        LOGGER.log(Level.INFO, "Input CE Id:{0}", httpHeaders.getFirst(ID));
        LOGGER.log(Level.INFO, "Input CE Spec Version:{0}", httpHeaders.getFirst(SPECVERSION));
        LOGGER.log(Level.INFO, "Input CE Source:{0}", httpHeaders.getFirst(SOURCE));
        LOGGER.log(Level.INFO, "Input CE Subject:{0}", httpHeaders.getFirst(SUBJECT));

        Input input = inputMessage.getPayload();
        LOGGER.log(Level.INFO, "Input {0} ", input);
    }

    @Override
    public String apply(Message<Input> inputMessage) {
        acceptImpl(inputMessage);
        return "ok";
    }
}
