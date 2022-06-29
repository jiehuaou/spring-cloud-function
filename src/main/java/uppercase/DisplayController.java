package uppercase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
public class DisplayController {

    @RequestMapping(value = "/display", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void display(@RequestHeader Map<String, String> headers,
                        @RequestBody String inputMessage) {
        //HttpHeaders httpHeaders = HeaderUtils.fromMessage(inputMessage.getHeaders());

        headers.entrySet().stream().filter(e -> e.getKey().startsWith("ce-")).forEach(e -> {
            log.info(" {} = {} ", e.getKey() ,e.getValue());
        });

//        Input input = inputMessage.getPayload();
        log.info("Input {} ", inputMessage);
    }
}
