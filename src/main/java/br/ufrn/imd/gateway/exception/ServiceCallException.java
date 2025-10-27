package br.ufrn.imd.gateway.exception;

public class ServiceCallException extends RuntimeException {
    public ServiceCallException(String message, Throwable cause) {
        super(message, cause);
    }
}