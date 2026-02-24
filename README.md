# Gate Service - Cartumio

## 📋 Sobre o Projeto

O **Gate Service** é um serviço leve e desacoplado do Cartumio, desenvolvido em **Spring Boot**, responsável pelas funcionalidades iniciais da plataforma. Este serviço gerencia o cadastro de usuários na fila de espera, confirmação de e-mail e gerenciamento de tokens de segurança.

### 🎯 Sobre o Cartumio

O **Cartumio** é um projeto de correio digital que resgata a experiência afetiva do envio e recebimento de cartas, inspirada nos tempos antigos. Diferente dos serviços tradicionais de e-mail, a plataforma não incentiva a comunicação imediata. Pelo contrário, valoriza o tempo, a espera e a intenção por trás de cada mensagem.

A proposta é oferecer uma **caixa de correio virtual**, onde mensagens são entregues de forma mais contemplativa, reforçando o sentimento de expectativa e cuidado que existia nas trocas de cartas físicas.

### 🎯 Objetivo do Gate Service

Fornecer um serviço robusto e escalável para:

- **Cadastro na Fila de Espera**: Gerenciamento de usuários interessados em se cadastrar na plataforma
- **Confirmação de E-mail**: Sistema de validação de e-mail através de tokens seguros
- **Gerenciamento de Tokens**: Criação, verificação e invalidação de tokens de segurança
- **Envio de E-mails**: Integração com Brevo para envio de e-mails transacionais
- **Rate Limiting**: Proteção contra abuso com controle de taxa de requisições
- **Suporte Multi-idioma**: Suporte a português (pt-BR) e inglês (en-US)

## 🛠️ Tecnologias e Dependências

### Stack Principal

- **Java 21** - Linguagem de programação
- **Spring Boot 4.0.2** - Framework principal
- **Spring Data JPA** - Abstração de acesso a dados
- **Hibernate** - ORM (Object-Relational Mapping)
- **PostgreSQL** - Banco de dados relacional
- **Flyway** - Controle de versão de banco de dados
- **RabbitMQ** - Sistema de mensageria para processamento assíncrono de e-mails
- **Brevo** - Serviço de envio de e-mails transacionais
- **Bucket4j** - Biblioteca para rate limiting
- **Mustache** - Template engine para e-mails
- **Lombok** - Redução de boilerplate
- **Bean Validation** - Validação de dados

### Dependências de Produção

```gradle
// Spring Boot Starters
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-flyway'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-webmvc'
implementation 'org.springframework.boot:spring-boot-starter-webflux'
implementation 'org.springframework.boot:spring-boot-starter-amqp'

// Database
implementation 'org.flywaydb:flyway-database-postgresql'
runtimeOnly 'org.postgresql:postgresql'

// Rate Limiting
implementation 'com.bucket4j:bucket4j-core:8.10.0'

// Email Templates
implementation 'com.github.spullara.mustache.java:compiler:0.9.10'

// Lombok
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

// Development
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

### Dependências de Teste

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.boot:spring-boot-test-autoconfigure'
testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
testImplementation 'org.springframework.boot:spring-boot-starter-flyway-test'
testImplementation 'org.springframework.boot:spring-boot-starter-validation-test'
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

## 📁 Estrutura do Projeto

```
src/main/java/com/cartumio/gate/
├── api/
│   ├── TokenController.java              # Endpoints de gerenciamento de tokens
│   └── WaitlistUserController.java       # Endpoints de fila de espera
├── config/
│   ├── email/
│   │   ├── BrevoConfig.java              # Configuração do cliente Brevo
│   │   └── BrevoProperties.java          # Propriedades do Brevo
│   ├── rabbitmq/
│   │   └── RabbitMQConfig.java           # Configuração do RabbitMQ
│   ├── ratelimit/
│   │   ├── RateLimitFilter.java          # Filtro de rate limiting
│   │   ├── RateLimitProperties.java      # Propriedades de rate limit
│   │   └── RateLimitService.java         # Serviço de rate limiting
│   └── WebConfig.java                    # Configurações web (CORS, etc.)
├── domain/
│   ├── AbstractEntity.java               # Entidade abstrata base
│   ├── email/
│   │   ├── Email.java                    # Entidade de e-mail
│   │   ├── EmailTemplate.java            # Template de e-mail
│   │   └── EmailUser.java                # Usuário do e-mail
│   ├── SystemLocale.java                 # Locale do sistema
│   ├── SystemTranslate.java             # Traduções do sistema
│   ├── token/
│   │   ├── Token.java                    # Entidade de token
│   │   └── TokenType.java                # Enum de tipos de token
│   └── WaitlistUser.java                 # Entidade de usuário da fila
├── dto/
│   ├── request/
│   │   ├── email/
│   │   │   ├── EmailRequest.java         # DTO de requisição de e-mail
│   │   │   └── EmailUserRequest.java     # DTO de usuário do e-mail
│   │   ├── token/
│   │   │   ├── TokenInvalidationRequest.java    # DTO de invalidação
│   │   │   └── TokenVerificationRequest.java    # DTO de verificação
│   │   ├── WaitlistUserConfirmationRequest.java # DTO de confirmação
│   │   └── WaitlistUserRequest.java      # DTO de cadastro na fila
│   └── response/
│       ├── EmailResponse.java            # DTO de resposta de e-mail
│       └── token/
│           ├── TokenResponse.java        # DTO de resposta de token
│           └── TokenVerificationResponse.java # DTO de verificação
├── exception/
│   ├── EmailFailedException.java        # Exceção de falha no envio
│   ├── ErrorResponse.java               # DTO de erro padronizado
│   └── GlobalExceptionHandler.java      # Handler global de exceções
├── job/
│   └── TokenCleanupJob.java             # Job de limpeza de tokens expirados
├── repository/
│   ├── EmailTemplateRepository.java     # Repositório de templates
│   ├── SystemLocaleRepository.java      # Repositório de locales
│   ├── TokenRepository.java             # Repositório de tokens
│   └── WaitlistUserRepository.java      # Repositório de usuários da fila
└── service/
    ├── email/
    │   ├── ConfirmationEmailService.java # Serviço de e-mail de confirmação
    │   ├── EmailConsumer.java           # Consumer de mensagens RabbitMQ
    │   ├── EmailProducer.java           # Producer de mensagens RabbitMQ
    │   ├── EmailService.java            # Serviço principal de e-mail
    │   └── EmailTemplateService.java    # Serviço de templates
    ├── SystemLocaleService.java         # Serviço de locales
    ├── token/
    │   └── TokenService.java            # Serviço de tokens
    └── WaitlistUserService.java         # Serviço de usuários da fila
```

## 🔐 Funcionalidades Principais

### Cadastro na Fila de Espera

- **Criação de Usuários**: Cadastro de novos usuários interessados na plataforma
- **Reenvio de E-mail**: Funcionalidade para reenviar e-mail de confirmação
- **Suporte Multi-idioma**: Suporte a português (pt-BR) e inglês (en-US)
- **Rate Limiting**: Proteção contra spam (3 e-mails por 5 minutos)

### Confirmação de E-mail

- **Geração de Tokens**: Criação de tokens seguros para confirmação
- **Validação de Tokens**: Verificação de validade e expiração
- **Invalidação Automática**: Tokens são invalidados após uso
- **Limpeza Automática**: Job agendado remove tokens expirados

### Gerenciamento de Tokens

- **Tipos de Token**: Sistema extensível para diferentes tipos de token
- **Expiração Configurável**: Tokens com tempo de expiração definido
- **Metadados**: Suporte a metadados JSON para informações adicionais
- **Rastreamento de Uso**: Registro de quando e como tokens foram consumidos

### Envio de E-mails

- **Processamento Assíncrono**: Uso de RabbitMQ para processamento em background
- **Templates Dinâmicos**: Sistema de templates usando Mustache
- **Integração Brevo**: Envio através da API do Brevo
- **Retry Automático**: Sistema de retry para falhas temporárias

### Rate Limiting

- **Proteção por Endpoint**: Regras específicas por endpoint
- **Bucket4j**: Implementação usando algoritmo token bucket
- **Configurável**: Regras definidas via configuração

## 🔧 Configurações

### Application Properties

O projeto utiliza apenas `application.yaml` (configuração unificada para todos os ambientes). As configurações principais:

```yaml
spring:
  application:
    name: gate
  profiles:
    active: ${SPRING_PROFILES_ACTIVE}
  datasource:
    url: ${POSTGRES_URL}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none  # Flyway gerencia o schema
    show-sql: true
    open-in-view: true
  flyway:
    enabled: true
    locations:
      - classpath:db/migration
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
    virtual-host: ${RABBITMQ_VIRTUAL_HOST}
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 1000
          max-retries: 3
          multiplier: 2
          max-interval: 10000

origin:
  base-url: ${ORIGIN_BASE_URL}

brevo:
  api-key: ${BREVO_API_KEY}
  base-url: ${BREVO_API_BASE_URL}
  sender:
    name: ${BREVO_DEFAULT_SENDER_NAME}
    email: ${BREVO_DEFAULT_SENDER_EMAIL}

ratelimit:
  default-rule:
    capacity: 10
    refill: 1m
  rules:
    - path: /gate/v1/waitlist-users/create-or-resend
      capacity: 3
      refill: 5m
```

### Variáveis de Ambiente

Um exemplo completo de variáveis está em `.devcontainer/.env.example`. Resumo:

#### Perfil
- `SPRING_PROFILES_ACTIVE`: Perfil ativo do Spring (ex.: `dev`)

#### Banco de Dados (PostgreSQL)
- `POSTGRES_USER`: Usuário do banco de dados
- `POSTGRES_PASSWORD`: Senha do banco de dados
- `POSTGRES_URL`: URL JDBC completa (ex.: `jdbc:postgresql://postgres:5432/cartumio_gate`)

#### RabbitMQ
- `RABBITMQ_HOST`: Hostname do RabbitMQ
- `RABBITMQ_PORT`: Porta AMQP (padrão: 5672)
- `RABBITMQ_MANAGEMENT_PORT`: Porta do Management UI (padrão: 15672)
- `RABBITMQ_USERNAME`: Usuário do RabbitMQ
- `RABBITMQ_PASSWORD`: Senha do RabbitMQ
- `RABBITMQ_VIRTUAL_HOST`: Virtual host do RabbitMQ (padrão: `/`)

#### Brevo
- `BREVO_API_KEY`: Chave de API do Brevo (obrigatório)
- `BREVO_API_BASE_URL`: URL base da API do Brevo
- `BREVO_DEFAULT_SENDER_NAME`: Nome do remetente padrão
- `BREVO_DEFAULT_SENDER_EMAIL`: E-mail do remetente padrão

#### Outras
- `ORIGIN_BASE_URL`: URL base da aplicação frontend (ex.: `http://localhost:3000`)

## 🧪 Testes

### Estrutura de Testes

O projeto possui uma suíte completa de testes utilizando **JUnit 5**:

#### Testes Unitários

- **WaitlistUserServiceTest**: Testa a lógica de negócio de usuários da fila
  - Criação de usuários
  - Confirmação de e-mail
  - Validação de dados
  - Reenvio de e-mail

- **TokenServiceTest**: Testa a lógica de gerenciamento de tokens
  - Criação de tokens
  - Verificação de validade
  - Invalidação de tokens
  - Expiração de tokens

- **EmailServiceTest**: Testa o serviço de e-mail
  - Envio de e-mails
  - Processamento de templates
  - Tratamento de erros

- **RateLimitServiceTest**: Testa o serviço de rate limiting
  - Aplicação de limites
  - Regras por endpoint
  - Reset de buckets

#### Testes de Integração

- **WaitlistUserControllerTest**: Testa endpoints de fila de espera
  - POST `/gate/v1/waitlist-users/create-or-resend` - Criação/reenvio
  - POST `/gate/v1/waitlist-users/confirm` - Confirmação
  - Validação de requisições
  - Tratamento de erros
  - Rate limiting

- **TokenControllerTest**: Testa endpoints de tokens
  - POST `/gate/v1/tokens/verify` - Verificação
  - POST `/gate/v1/tokens/invalidate` - Invalidação
  - Validação de tokens
  - Tratamento de erros

- **EmailConsumerTest**: Testa o consumer de mensagens RabbitMQ
  - Processamento de mensagens
  - Tratamento de falhas
  - Retry automático

### Executando os Testes

```bash
# Executar todos os testes
./gradlew test

# Executar testes com relatório
./gradlew test --info

# Executar apenas testes unitários
./gradlew test --tests "*ServiceTest"

# Executar apenas testes de integração
./gradlew test --tests "*ControllerTest"
```

## 📡 Endpoints da API

### WaitlistUser (Fila de Espera)

#### POST `/gate/v1/waitlist-users/create-or-resend`
Cria um novo usuário na fila de espera ou reenvia o e-mail de confirmação.

**Headers:**
```
Accept-Language: pt-BR | en-US
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

**Response (200 OK ou 201 Created):**
```json
{
  "message": "E-mail de confirmação enviado com sucesso",
  "success": true
}
```

**Rate Limit:** 3 requisições por 5 minutos

#### POST `/gate/v1/waitlist-users/confirm`
Confirma o e-mail do usuário e invalida o token de confirmação.

**Request Body:**
```json
{
  "token": "token-de-confirmacao"
}
```

**Response (200 OK):**
```json
{
  "message": "E-mail confirmado com sucesso",
  "confirmed": true
}
```

### Token (Gerenciamento de Tokens)

#### POST `/gate/v1/tokens/verify`
Verifica se um token ainda é válido.

**Request Body:**
```json
{
  "token": "token-de-confirmacao",
  "tokenType": "EMAIL_CONFIRMATION"
}
```

**Response (200 OK):**
```json
{
  "valid": true,
  "expiresAt": "2024-01-01T12:00:00Z",
  "consumed": false
}
```

#### POST `/gate/v1/tokens/invalidate`
Invalida um token específico.

**Request Body:**
```json
{
  "token": "token-de-confirmacao",
  "tokenType": "EMAIL_CONFIRMATION"
}
```

**Response (200 OK):**
```json
{
  "message": "Token invalidado com sucesso",
  "invalidated": true
}
```

## 🚨 Tratamento de Exceções

O projeto implementa um **GlobalExceptionHandler** que centraliza o tratamento de todas as exceções:

### Exceções Customizadas

- `EmailFailedException`: Falha no envio de e-mail

### Respostas de Erro Padronizadas

Todas as exceções retornam um formato padronizado:

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Mensagem de erro descritiva",
  "path": "/gate/v1/waitlist-users/create-or-resend"
}
```

## 📝 Logging

O projeto utiliza **SLF4J com Lombok** para logging estruturado:

- **@Slf4j**: Anotação do Lombok que gera automaticamente o logger
- **Níveis de Log**:
  - `log.info()`: Operações importantes (cadastro, confirmação)
  - `log.debug()`: Informações detalhadas para debug
  - `log.warn()`: Avisos (rate limit atingido, tokens inválidos)
  - `log.error()`: Erros inesperados

### Exemplo de Logs

```
INFO  - Novo usuário cadastrado na fila: john.doe@example.com
INFO  - E-mail de confirmação enviado para: john.doe@example.com
WARN  - Rate limit atingido para endpoint: /gate/v1/waitlist-users/create-or-resend
ERROR - Falha ao enviar e-mail: Connection timeout
```

## 🗄️ Banco de Dados

### Migrações Flyway

O projeto utiliza **Flyway** para versionamento do banco de dados:

- `V001__create_system_locales_table.sql`: Criação da tabela de locales
- `V002__create_system_translates_table.sql`: Criação da tabela de traduções
- `V003__create_waitlist_users_table.sql`: Criação da tabela de usuários da fila
- `V004__create_email_templates_table.sql`: Criação da tabela de templates de e-mail
- `V005__insert_initial_system_locales.sql`: Inserção de locales iniciais
- `V006__create_tokens_table.sql`: Criação da tabela de tokens
- `V007__insert_confirmation_email_templates.sql`: Inserção de templates de confirmação

### Modelo de Dados

- **WaitlistUser**: Entidade principal de usuário da fila
  - Campos: id, firstName, lastName, email, isConfirmed, systemLocaleId, createdAt, updatedAt

- **Token**: Entidade de tokens de segurança
  - Campos: id, token, tokenType, expiresAt, consumedAt, isConsumed, metadata, createdAt, updatedAt

- **EmailTemplate**: Templates de e-mail
  - Campos: id, name, subject, body, localeId, createdAt, updatedAt

- **SystemLocale**: Locales suportados
  - Campos: id, code, name, createdAt, updatedAt

- **SystemTranslate**: Traduções do sistema
  - Campos: id, localeId, key, value, createdAt, updatedAt

## 🔒 Segurança Implementada

### Rate Limiting

- **Bucket4j**: Implementação usando algoritmo token bucket
- **Regras Configuráveis**: Limites específicos por endpoint
- **Proteção contra Abuso**: Prevenção de spam e ataques de força bruta

### Validações

- **Bean Validation**: Validação de dados de entrada
- **Custom Validators**: Validações específicas de negócio
- **Constraint Violations**: Tratamento de violações de constraints

### Processamento Assíncrono

- **RabbitMQ**: Mensageria para processamento de e-mails em background
- **Retry Automático**: Sistema de retry para falhas temporárias
- **Resiliência**: Isolamento de falhas no processamento de e-mails

## 🚀 Como Usar Este Projeto

### 1. Pré-requisitos

- Java 21 ou superior
- PostgreSQL 13 ou superior
- RabbitMQ 3.8 ou superior
- Gradle 8.0 ou superior

### 2. Clonar e Configurar

```bash
git clone <repository-url>
cd cartumio-gate
```

### 3. Configurar Variáveis de Ambiente

Copie `.devcontainer/.env.example` para `.env` (ou para o local onde o projeto lê variáveis) e ajuste os valores. Exemplo:

```bash
# Perfil
export SPRING_PROFILES_ACTIVE=dev

# PostgreSQL (URL completa)
export POSTGRES_USER=cartumio_user
export POSTGRES_PASSWORD=cartumio_password
export POSTGRES_URL=jdbc:postgresql://localhost:5432/cartumio_gate

# Brevo
export BREVO_API_KEY=sua_chave_api_brevo
export BREVO_API_BASE_URL=https://api.brevo.com/v3
export BREVO_DEFAULT_SENDER_NAME=Cartumio
export BREVO_DEFAULT_SENDER_EMAIL=noreply@cartumio.com

# Origin (frontend)
export ORIGIN_BASE_URL=http://localhost:3000

# RabbitMQ
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_MANAGEMENT_PORT=15672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
export RABBITMQ_VIRTUAL_HOST=/
```

**Desenvolvimento com Dev Container:** o repositório inclui um Dev Container (`.devcontainer/`) com Docker Compose que sobe PostgreSQL e RabbitMQ. Abra o projeto no VS Code/Cursor com “Reopen in Container”; use o `.env.example` como referência (no container, `POSTGRES_URL` deve apontar para o serviço `postgres`, ex.: `jdbc:postgresql://postgres:5432/cartumio_gate`).

### 4. Executar Migrações

As migrações Flyway são executadas automaticamente na inicialização.

### 5. Iniciar a Aplicação

```bash
./gradlew bootRun
```

### 6. Testar os Endpoints

```bash
# Cadastrar usuário na fila (Português)
curl -X POST http://localhost:8080/gate/v1/waitlist-users/create-or-resend \
  -H "Content-Type: application/json" \
  -H "Accept-Language: pt-BR" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com"}'

# Cadastrar usuário na fila (Inglês)
curl -X POST http://localhost:8080/gate/v1/waitlist-users/create-or-resend \
  -H "Content-Type: application/json" \
  -H "Accept-Language: en-US" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com"}'

# Confirmar e-mail
curl -X POST http://localhost:8080/gate/v1/waitlist-users/confirm \
  -H "Content-Type: application/json" \
  -d '{"token":"token-recebido-por-email"}'

# Verificar token
curl -X POST http://localhost:8080/gate/v1/tokens/verify \
  -H "Content-Type: application/json" \
  -d '{"token":"token-recebido-por-email","tokenType":"EMAIL_CONFIRMATION"}'
```

## 📦 Extensibilidade

Este projeto pode ser facilmente estendido com:

- **Novos Tipos de Token**: Adicionar novos tipos em `TokenType`
- **Novos Templates de E-mail**: Criar templates em `EmailTemplate`
- **Novos Locales**: Adicionar suporte a novos idiomas
- **Novos Endpoints**: Adicionar controllers na pasta `api`
- **Novas Funcionalidades**: Implementar serviços em `service`
- **Novas Regras de Rate Limit**: Configurar regras em `application.yaml`

## 🔄 Jobs Agendados

O projeto utiliza **Spring Scheduling** para executar tarefas agendadas:

- **TokenCleanupJob**: Remove tokens expirados do banco de dados periodicamente

## 📄 Licença

Este projeto está sob licença. Consulte o arquivo `LICENSE` para mais detalhes.

## 👥 Contribuindo

Este é um projeto do Cartumio. Para contribuir:

1. Faça fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📚 Recursos Adicionais

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Brevo API Documentation](https://developers.brevo.com/)
- [Bucket4j Documentation](https://bucket4j.com/)

---
