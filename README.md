# 🏢 Plataforma Reativa de Reserva de Salas & Processamento em Lote

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.4.3](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)](https://spring.io/projects/spring-boot)
[![Quarkus 3.19.1](https://img.shields.io/badge/Quarkus-3.19.1-blue.svg)](https://quarkus.io/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)](https://redis.io/)
[![OpenTelemetry](https://img.shields.io/badge/Observability-OpenTelemetry%2FJaeger-purple.svg)](https://opentelemetry.io/)

Plataforma distribuída e reativa para gestão, consulta de disponibilidade, agendamento de salas de reunião e sugestão inteligente de horários. O sistema é composto por microserviços em **Spring Boot (WebFlux, Batch, JPA, Redis)** e **Quarkus (GraalVM Native, JAX-RS)**, com streaming de eventos em tempo real (**SSE**), processamento de dados em lote resiliente e rastreamento distribuído (**OpenTelemetry/Jaeger**).

---

## 📐 1. Arquitetura do Sistema

```
                         ┌─────────────────────────────────────────────────┐
                         │               Cliente / Front-End               │
                         └───────────────────────┬─────────────────────────┘
                                                 │
                        ┌────────────────────────┴────────────────────────┐
                        │                                                 │
                        ▼ (HTTP / WebFlux)                                ▼ (HTTP / REST)
       ┌─────────────────────────────────┐               ┌─────────────────────────────────┐
       │         booking-service         │               │        sugestao-service         │
       │    (Spring Boot 3.4 / WebFlux)  │               │      (Quarkus 3.19 / REST)      │
       │  Porta: 8081                    │               │  Porta: 8082                    │
       └───┬─────────────┬─────────────┬─┘               └────────────────┬────────────────┘
           │             │             │                                  │
           │             │             └──────────────────────────────────┘
           ▼             ▼                       (REST Client)
   ┌──────────────┐ ┌─────────────┐
   │ Redis Cache  │ │  H2 / SQL   │
   │ (Porta 6379) │ │ (In-Memory) │
   └──────────────┘ └─────────────┘
           ▲
           │ (Spans / Traces OTLP)
   ┌──────────────┐
   │ Jaeger APM   │
   │ (Porta 16686)│
   └──────────────┘
```

### Componentes Principais:
1. **`booking-service` (Spring Boot 3.4 / Java 21)**:
   - **Reactive Endpoints (WebFlux)**: APIs não-bloqueantes com isolamento de chamadas JPA em `Schedulers.boundedElastic()`.
   - **Server-Sent Events (SSE)**: Streaming reativo de eventos de confirmação, cancelamento e expiração no endpoint `/api/reservas/events/stream`.
   - **Spring Batch Job `ManutençãoDiária`**: Steps desacoplados via `@StepScope` para expiração automática de reservas pendentes, geração de lembretes (24h antecedência) e consolidação analítica de ocupação diária.
   - **Cache com Redis**: Estratégias `@Cacheable` e `@CacheEvict` com namespaces isolados (`rooms`, `availability`) e tratamento gracioso de falha (`CacheErrorHandler`).
   - **Observabilidade**: Métricas customizadas Micrometer (`reservas.criadas`, `reservas.conflitos`, `redis.cache.hits`) e rastreamento distribuído OpenTelemetry enviado ao Jaeger.

2. **`sugestao-service` (Quarkus 3.19 / Java 21)**:
   - **Microserviço de Sugestão**: Algoritmo de recomendação multicritério (compatibilidade de capacidade, localização e score de conflito zero).
   - **REST Client Reativo**: Integração resiliente via MicroProfile REST Client chamando o `booking-service`.
   - **GraalVM Native**: Suporte a compilação nativa com inicialização ultra-rápida.
   - **Emissão de Eventos**: Emite o evento `SugestaoAceitaEvent` quando uma sugestão é confirmada.

---

## 🛠️ 2. Instalação e Requisitos

### Pré-requisitos:
- **Java 21 (OpenJDK / Eclipse Temurin)**
- **Apache Maven 3.9+**
- **Docker & Docker Compose** (para execução containerizada)

### Clonar o repositório:
```bash
git clone https://github.com/empresa/reserva-salas.git
cd reserva-salas
```

---

## ⚙️ 3. Configuração

### Variáveis de Ambiente

| Variável | Descrição | Valor Padrão |
| :--- | :--- | :--- |
| `REDIS_HOST` | Endereço do servidor Redis | `localhost` |
| `REDIS_PORT` | Porta de conexão do Redis | `6379` |
| `QUARKUS_REST_CLIENT_BOOKING_SERVICE_API_URL` | Endpoint base do `booking-service` para o Quarkus | `http://localhost:8081` |
| `MANAGEMENT_OTLP_TRACING_ENDPOINT` | Endpoint gRPC do OTLP Exporter no Jaeger | `http://localhost:4317` |

---

## 🚀 4. Execução & Uso

### Opção A: Execução via Docker Compose (Recomendado)
Para subir a infraestrutura completa (Redis, Jaeger, Booking Service e Sugestão Service):

```bash
docker-compose up -d --build
```

Serviços disponíveis:
- **Booking Service**: `http://localhost:8081`
- **Sugestão Service (Quarkus)**: `http://localhost:8082`
- **Jaeger Web UI**: `http://localhost:16686`
- **Console H2**: `http://localhost:8081/h2-console`

---

### Opção B: Execução Local com Maven

1. **Iniciar o `booking-service`**:
```bash
mvn spring-boot:run -f booking-service/pom.xml
```

2. **Iniciar o `sugestao-service` (Quarkus)**:
```bash
mvn quarkus:dev -f sugestao-service/pom.xml
```

---

### 📥 Exemplos de Requisições REST

#### 1. Consultar Disponibilidade Reativa (WebFlux)
```bash
curl -X GET "http://localhost:8081/api/salas/disponibilidade?minCapacity=10"
```

#### 2. Streaming de Eventos em Tempo Real (SSE)
```bash
curl -N -H "Accept: text/event-stream" "http://localhost:8081/api/reservas/events/stream"
```

#### 3. Criar uma Reserva
```bash
curl -X POST "http://localhost:8081/api/reservas" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "userEmail": "diretoria@empresa.com",
    "startTime": "2026-08-22T14:00:00",
    "endTime": "2026-08-22T15:30:00"
  }'
```

#### 4. Disparar Job Spring Batch Manualmente
```bash
curl -X POST "http://localhost:8081/api/batch/jobs/manutencao"
```

#### 5. Solicitar Sugestão de Horários (Quarkus Microservice)
```bash
curl -X POST "http://localhost:8082/api/sugestoes" \
  -H "Content-Type: application/json" \
  -d '{
    "minCapacity": 8,
    "requiredLocation": "Bloco A",
    "durationMinutes": 60
  }'
```

#### 6. Aceitar Sugestão de Sala
```bash
curl -X POST "http://localhost:8082/api/sugestoes/aceitar?roomId=1&roomName=Sala%20Inova%C3%A7%C3%A3o&userEmail=diretoria@empresa.com&score=95.5" \
  -H "Content-Type: application/json"
```

---

## 🧪 5. Testes Automatizados

A suíte de testes cobre endpoints reativos, streaming SSE, execução de jobs em lote, resiliência do cache Redis e cálculo do algoritmo de sugestão do Quarkus.

### Executar testes do `booking-service`:
```bash
mvn clean test -f booking-service/pom.xml
```

### Executar testes do `sugestao-service` (Quarkus):
```bash
mvn clean test -f sugestao-service/pom.xml
```

---

## 🔒 6. Segurança & Resiliência

- **Isolamento Bounded Elastic**: Chamadas JDBC blocking são estritamente isoladas no thread-pool `Schedulers.boundedElastic()`, protegendo o Event Loop do WebFlux contra starvation.
- **Fail-Safe Cache Error Handler**: Se o servidor Redis estiver offline, a aplicação intercepta a exceção e redireciona a consulta transparentemente para o banco de dados sem quebrar a requisição HTTP do usuário.
- **Validação de Sobreposição**: A criação de reservas executa verificações atômicas de intervalo de horário (`startTime < newEnd AND endTime > newStart`), rejeitando conflitos com status HTTP 409 CONFLICT.
- **Tratamento Reativo Globais de Exceções**: Respostas estruturadas no padrão RFC 7807 problem details via `@RestControllerAdvice`.

---

## 🔍 7. Troubleshooting

### 1. Falha de Conexão com Redis (`Unable to connect to Redis`)
- **Causa**: O container Redis não foi iniciado na porta `6379`.
- **Solução**: O serviço conta com `CacheErrorHandler` resiliente e continuará respondendo via banco de dados H2/SQL. Para subir o Redis localmente: `docker run -d -p 6379:6379 redis:7.2-alpine`.

### 2. Conflito de Porta (`Port 8081 or 8082 already in use`)
- **Solução**: Finalize o processo na porta correspondente:
  ```bash
  kill $(lsof -t -i :8081) 2>/dev/null || true
  kill $(lsof -t -i :8082) 2>/dev/null || true
  ```
