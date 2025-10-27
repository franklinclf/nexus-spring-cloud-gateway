package br.ufrn.imd.gateway.exception;

public record ErrorResponse(String error, String message, String details){}