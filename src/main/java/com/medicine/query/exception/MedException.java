package com.medicine.query.exception;


import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(chain = true)
public class MedException extends RuntimeException {

    public MedException(Exception e) {
        super(e);
    }

    public MedException(String e) {
        super(e);
    }

}
