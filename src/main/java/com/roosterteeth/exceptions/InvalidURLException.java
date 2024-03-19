package com.roosterteeth.exceptions;

public class InvalidURLException extends Exception{

    public InvalidURLException(String errorMessage){
        super(errorMessage);
    }
}
