package com.banka1.banking.bootstrap;

import com.banka1.banking.dto.CreateEventDTO;
import com.banka1.banking.models.Event;
import com.banka1.banking.services.CurrencyService;
import com.banka1.banking.services.EventExecutorService;
import com.banka1.banking.services.EventService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapExchangeRateLoader implements CommandLineRunner {

    private final CurrencyService currencyService;
    private final EventService eventService;
    private final EventExecutorService eventExecutorService;

    public BootstrapExchangeRateLoader(CurrencyService currencyService,
                                       EventService eventService,
                                       EventExecutorService eventExecutorService) {
        this.currencyService = currencyService;
        this.eventService = eventService;
        this.eventExecutorService = eventExecutorService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Fetching exchange rates on startup ===");
        currencyService.fetchExchangeRates();
        System.out.println("=== Exchange rates fetched successfully ===");


//         test events

        System.out.println("=== Testing event creation ===");

//        CreateEventDTO createEventDTO = new CreateEventDTO();
//        event.setMessageType("TEST");
//        event.setUrl("http://localhost:8080/api/v1/events");
//        event.setPayload("{\"test\":\"test\"}");

        Event event = eventService.createEvent(new CreateEventDTO(
                "TEST",
                "{\"test\":\"test\"}",
                "http://localhost:8082/interbank"
        ));

        System.out.println("=== Executing event ===");

        eventExecutorService.attemptEventAsync(event);

        System.out.println("=== Event executed successfully ===");
    }
}
