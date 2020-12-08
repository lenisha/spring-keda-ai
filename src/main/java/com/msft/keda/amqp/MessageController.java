package com.msft.keda.amqp;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.RemoteDependencyTelemetry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
public class MessageController {

    private static Logger logger = LoggerFactory.getLogger(MessageController.class);

    private String RESOURCE_URL="https://api.github.com/";

    // Micrometer Metrics
    final Counter counter = Metrics.counter("servicebus_msg_counter");
    final Timer timer = Metrics.timer("servicebus_msg_timer");

    //  SDK only to send custom Dependency metrics
    // https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-in-process-agent
    private static TelemetryClient telemetryClient = new TelemetryClient();

    @Autowired
    private MessageRepository msgRepository;

    @Autowired
    private RestTemplate restTemplate;


    @RequestMapping(value = "/", produces = { "application/json"})
    public Iterable<Message> index() {
        return  msgRepository.findAll();

    }

    @Timed("servicebus_msg_timer")
    @PostMapping(value = "/savemessage",
                produces = { "application/json"},
                consumes = { "application/json"})
    public String save(@RequestBody Message message) {
        Message msg = null;

        long msStart = System.currentTimeMillis();
        try {
            // SAVE to MYSQL
            msg = msgRepository.save(message);

            // CALL to EXTERNAL API
            ResponseEntity<String> response = restTemplate.getForEntity(RESOURCE_URL, String.class);
            logger.info("API : {0}", response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            // custom telemetry example
            telemetryClient.trackException(e);

        } finally {
            // custom dependency - just an example
            RemoteDependencyTelemetry telemetry = new RemoteDependencyTelemetry("ServiceBus");
            telemetry.setTimestamp(new Date(msStart));
            telemetry.setDuration(new Duration(System.currentTimeMillis() - msStart));
            telemetryClient.trackDependency(telemetry);
        }

        // increment micrometer metrics
        counter.increment();
        timer.record(System.currentTimeMillis() - msStart, TimeUnit.MILLISECONDS);

        return msg.getId().toString();
    }
}
