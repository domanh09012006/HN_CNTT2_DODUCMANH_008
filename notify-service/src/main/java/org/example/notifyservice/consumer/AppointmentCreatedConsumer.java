package org.example.notifyservice.consumer;

import org.example.notifyservice.service.EmailService;

public class AppointmentCreatedConsumer {
    private final EmailService emailService;

    public AppointmentCreatedConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    public void consume(String email) {
        throw new UnsupportedOperationException();
    }
}
