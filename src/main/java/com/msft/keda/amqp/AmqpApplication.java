package com.msft.keda.amqp;

import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.RemoteDependencyTelemetry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.microsoft.applicationinsights.TelemetryClient;


import java.util.Date;


@SpringBootApplication
public class AmqpApplication  {

	private static Logger logger = LoggerFactory.getLogger(AmqpApplication.class);

	@Autowired
	private ServiceBusReceiverAsyncClient queueReceiver;
	@Autowired
	private RestTemplate restTemplate;



	public static void main(String[] args) {
		logger.info("Listening to Messages ");
		SpringApplication.run(AmqpApplication.class, args);
	}

	@Bean

	public CommandLineRunner commandLineRunner(ApplicationContext ctx)  {
		return args -> {
			receiveQueueMessage();
		};
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}


	private void receiveQueueMessage() {

		queueReceiver.receiveMessages().subscribe(message -> {
			// Process message. The message lock is renewed for up to 1 minute.
			System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(), message.getBody());
			logger.info("Sequence #: {0}. Contents: {1}", message.getSequenceNumber(), message.getBody());

			// Invoke Controller (to see the denedencies)
			HttpHeaders headers=new HttpHeaders();
			headers.set("Content-Type", "application/json");
			HttpEntity<Message> request = new HttpEntity<>(new Message(message.getBody().toString()), headers);
			restTemplate.postForObject("http://localhost:8080/savemessage", request, String.class);
		});
	}



}
