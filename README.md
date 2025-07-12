# Nexus Support - Gateway

![Java](https://img.shields.io/badge/Java-24-blue?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.3-green?logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.0.0-blueviolet?logo=spring&logoColor=white)
![Spring Cloud Gateway](https://img.shields.io/badge/Gateway_WebFlux-Reactive-orange?logo=spring&logoColor=white)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-lightgrey)

---

## 📚 Visão Geral do Projeto

Este repositório faz parte do projeto acadêmico Nexus Support, desenvolvido para a disciplina DIM0614 - Programação Distribuída na UFRN. O Nexus Support é um sistema de suporte distribuído que visa otimizar a gestão de chamados (tickets) utilizando uma arquitetura de microsserviços e inteligência artificial.

### Função do Gateway:

O **Gateway** atua como **ponto de entrada único** para todas as requisições da arquitetura. Ele é responsável por:

* Roteamento inteligente de requisições para os microsserviços corretos.
* Balanceamento de carga entre instâncias.
* Aplicação de filtros reativos.
* Observabilidade e rastreamento distribuído.
* Suporte a tolerância a falhas com circuit breakers.

---

## 🚀 Tecnologias Utilizadas

O serviço é construído sobre o stack moderno e reativo do ecossistema Spring:

* **Spring Boot (3.5.3):** Framework principal para bootstrapping da aplicação.
* **Java (24):** Linguagem utilizada.
* **Spring Cloud (2025.0.0):** Para construção de microsserviços modernos:
    * `spring-cloud-starter-gateway-server-webflux`: Implementa o Gateway reativo com Spring WebFlux.
    * `spring-cloud-starter-netflix-eureka-client`: Permite que o Gateway descubra os microsserviços dinamicamente.
    * `spring-cloud-starter-loadbalancer`: Balanceamento de carga do lado do cliente.
    * `spring-cloud-starter-config`: Integração com o Config Server.
    * `spring-cloud-starter-circuitbreaker-reactor-resilience4j`: Para implementar tolerância a falhas com circuit breaker.
* **Spring WebFlux:** Modelo de programação reativa, ideal para o desempenho do Gateway.
* **Micrometer Tracing & Zipkin:** Para rastreamento distribuído de requisições.
* **Prometheus:** Para exportação de métricas e monitoramento.
* **Spring Boot Actuator:** Para endpoints administrativos e visibilidade operacional.
* **Spring Boot DevTools:** Suporte ao desenvolvimento ágil.

---

## ⚙️ Funcionalidades

* 🔀 **Roteamento Dinâmico:** Redirecionamento inteligente de requisições baseado em regras configuráveis.
* ⚖️ **Balanceamento de Carga:** Utiliza descoberta de serviços e load balancer para distribuir requisições.
* 🧠 **Circuit Breaker Reativo:** Implementado com Resilience4j para proteção contra falhas em microsserviços.
* 🛡️ **Segurança, Logs e Filtros:** Suporte a filtros customizados para autenticação, logs ou rate limiting (extensível).
* 📊 **Observabilidade:** Integrado com Prometheus e Zipkin para métricas e tracing distribuído.

---

## 📈 Monitoramento e Observabilidade

Este serviço está configurado para fornecer visibilidade total por meio de:

* **Actuator:** Endpoints operacionais disponíveis em `/actuator`.
* **Prometheus:** Métricas exportadas no formato Prometheus via `/actuator/prometheus`.
* **Zipkin:** Integração com rastreamento distribuído através do Micrometer Tracing.

---

## 🗺️ Outros Repositórios do Nexus Support

Este serviço faz parte de um ecossistema distribuído. Veja os demais componentes:

* ⚙️ **[Nexus Support - Config Server](https://github.com/franklinclf/nexus-spring-cloud-config):** Gerenciamento centralizado de configurações.
* 🔍 **[Nexus Support - Eureka Discovery Service](https://github.com/franklinclf/nexus-spring-cloud-discovery):** Registro e descoberta de serviços.
* 🧠 **[Nexus Support - AI Service](https://github.com/franklinclf/nexus-spring-cloud-ai):** Triagem inteligente de tickets com IA.
* 🔗 **[Nexus Support - Ticket Service + MCP](https://github.com/franklinclf/nexus-spring-cloud-mcp):** Operações e análise de tickets integradas com IA.
* ☁️ **[Nexus Support - Serverless Function](https://github.com/franklinclf/nexus-spring-cloud-serverless):** Geração de relatórios e tarefas pontuais em ambiente serverless.

---
