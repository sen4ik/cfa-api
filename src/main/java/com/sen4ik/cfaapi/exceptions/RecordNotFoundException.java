package com.sen4ik.cfaapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(int id) {
        super("Could not find record with id " + id);
    }

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException() {
        super("Could not find record");
    }

}
