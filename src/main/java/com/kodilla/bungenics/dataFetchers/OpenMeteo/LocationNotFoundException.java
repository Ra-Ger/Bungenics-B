package com.kodilla.bungenics.dataFetchers.OpenMeteo;

import com.kodilla.bungenics.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Location not found or invalid format")
public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(String message) {
        super(message);
    }
}