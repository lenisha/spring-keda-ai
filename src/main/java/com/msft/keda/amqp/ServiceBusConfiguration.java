package com.msft.keda.amqp;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.time.Duration;

import static com.azure.messaging.servicebus.models.ServiceBusReceiveMode.PEEK_LOCK;

@Configuration
@EnableConfigurationProperties(ServiceBusProperties.class)
public class ServiceBusConfiguration {

    private final ServiceBusProperties properties;

    public ServiceBusConfiguration(ServiceBusProperties properties) {
        this.properties = properties;
    }


    @Bean
    public ServiceBusReceiverAsyncClient queueReceiver() {
        return new ServiceBusClientBuilder()
                .connectionString(properties.getConnectionString())
                .receiver()
                .receiveMode(properties.getQueueReceiveMode() == null ? PEEK_LOCK : properties.getQueueReceiveMode())
                .queueName(properties.getQueueName())
                //.maxAutoLockRenewDuration(Duration.ofMinutes(1))
                .buildAsyncClient();
    }



}