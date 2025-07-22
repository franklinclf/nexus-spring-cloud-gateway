package br.ufrn.imd.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/servico-indisponivel")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) // Retorna HTTP 503
    public Mono<Map<String, String>> serviceUnavailable() {
        // Retorna um Mono, pois o Spring Cloud Gateway é reativo
        return Mono.just(Map.of(
                "codigo", "ERRO_INTEGRACAO",
                "mensagem", "O serviço solicitado está temporariamente indisponível. Por favor, tente novamente mais tarde."
        ));
    }
}