package com.msft.keda.amqp;
import org.springframework.data.annotation.Id;

public class Message {

    public Message() {
    }

    public Message(String description) {
        this.description = description;
    }

    @Id
    private Long id;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
