package org.example.notifyservice.consumer;

import org.example.notifyservice.service.EmailService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReservationCreatedConsumerTests {

    @Test
    void delegatesMessageToEmailService() {
        EmailService emailService = mock(EmailService.class);
        ReservationCreatedConsumer consumer = new ReservationCreatedConsumer(emailService);

        consumer.consume("customer@example.com");

        verify(emailService).sendReservationCreatedEmail("customer@example.com");
    }
}
