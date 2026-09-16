# ATLAS Core API

API central do ATLAS para sessões de diagnóstico, decisões de arquitetura e orquestração do serviço desacoplado de inteligência.

## Stack

- Java 21 e Spring Boot 4.1.1
- Spring MVC, Validation e Problem Details
- Spring Data JPA, PostgreSQL e Flyway
- OAuth2 Resource Server com JWT
- Spring Modulith para proteger os limites internos
- RestClient para integração com `atlas-intelligence-service`
- Actuator, Prometheus, tracing e OpenTelemetry
- OpenAPI/Swagger UI
- Testcontainers e JUnit 5

## Executar localmente

Pré-requisitos: Java 21 e Docker com Docker Compose.

```bash
cp .env.example .env
docker compose up -d
./mvnw spring-boot:run
```

O perfil padrão permite chamadas sem autenticação para facilitar o desenvolvimento local. Para habilitar JWT, defina `ATLAS_SECURITY_ENABLED=true` e configure o provedor OAuth2 por variáveis/propriedades do Spring Security.

Endpoints iniciais:

- Status: `GET http://localhost:8080/api/v1/status`
- Health: `GET http://localhost:8080/actuator/health`
- Métricas: `GET http://localhost:8080/actuator/prometheus`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Grafana local: `http://localhost:3000`

## Validar

```bash
./mvnw verify
```

Os testes de contexto usam PostgreSQL real via Testcontainers. O Docker precisa estar disponível.

## Organização inicial

```text
br.com.lucascoliveira.atlas.core
├── decision       # sessões de diagnóstico e decisões arquiteturais
├── intelligence   # porta para IA, RAG e MCP desacoplados
├── config         # segurança e clientes externos
└── status         # verificação simples da API
```

O banco começa com a migration `V1__create_diagnostic_session.sql`. As próximas funcionalidades devem entrar por módulos de negócio, evitando acoplamento direto do domínio com provedores de IA.

## Variáveis principais

| Variável | Uso | Padrão local |
| --- | --- | --- |
| `DB_URL` | JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/atlas` |
| `DB_USERNAME` | usuário do banco | `atlas` |
| `DB_PASSWORD` | senha do banco | `atlas` |
| `ATLAS_SECURITY_ENABLED` | habilita JWT | `false` |
| `ATLAS_INTELLIGENCE_URL` | URL do serviço de inteligência | `http://localhost:8000` |
| `TRACING_SAMPLE_PROBABILITY` | amostragem de traces | `1.0` |

Nunca versione segredos reais; use `.env` apenas no ambiente local e um gerenciador de segredos nos ambientes publicados.
