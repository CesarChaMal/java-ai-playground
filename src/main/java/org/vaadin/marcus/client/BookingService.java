package org.vaadin.marcus.client;

import org.springframework.stereotype.Component;
import org.vaadin.marcus.service.BookingDetails;
import org.vaadin.marcus.service.FlightService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

import java.util.List;

@Component
@BrowserCallable
@AnonymousAllowed
public class BookingService {
    private final FlightService carRentalService;

    public BookingService(FlightService carRentalService) {
        this.carRentalService = carRentalService;
    }

    public List<BookingDetails> getBookings() {
        return carRentalService.getBookings();
    }
}
