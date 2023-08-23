package com.legends.promiscuous.exceptions;

public enum ExceptionMessage {

    USER_NOT_FOUND_EXCEPTION("User not found"),
    USER_WITH_EMAIL_NOT_FOUND_EXCEPTION("User with email %s not found"),
    USER_REGISTRATION_FAILED_EXCEPTION("User registration failed"),
    ACCOUNT_ACTIVATION_FAILED_EXCEPTION("Account activation was not successful"),

     BAD_CREDENTIALS_EXCEPTION("invalid authentication credentials");

    private final String message;

    ExceptionMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }


}
