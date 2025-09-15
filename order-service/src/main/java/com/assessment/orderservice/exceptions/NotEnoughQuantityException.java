package com.assessment.orderservice.exceptions;

public class NotEnoughQuantityException extends RuntimeException{

    public NotEnoughQuantityException(String massage){
        super(massage);
    }
}
