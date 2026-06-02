# FestConnect API

API REST de marketplace que conecta organizadores de eventos a prestadores de serviços DJs, buffets, aluguel de equipamentos e espaços para eventos.

---

## Sobre o projeto

O FestConnect resolve um problema concreto: encontrar prestadores confiáveis para um evento sem depender de indicação boca a boca. Clientes descrevem o evento, buscam prestadores por categoria ou proximidade, solicitam orçamentos e fecham negócio diretamente via WhatsApp  sem intermediação de pagamento.

O backend gerencia dois perfis distintos: **clientes** (que buscam e solicitam) e **prestadores** (que cadastram serviços e respondem orçamentos).

---

## Funcionalidades

- **Autenticação**: login via JWT com hash BCrypt, recuperação de senha por código PIN e login social com Google OAuth2
- **Perfis de prestador**: endereço completo, geolocalização automática via Nominatim e contato por WhatsApp
- **Catálogo de itens**: modelado com herança JOINED — `Produto`, `Servico` e `Local` estendem a entidade base `ItemCatalogo`
- **Busca por proximidade**: filtragem de prestadores por raio em km usando lat/lng e fórmula de Haversine
- **Pedidos e orçamentos**: cliente solicita com dados do evento, prestador responde com valor e detalhes; negociação segue pelo WhatsApp
- **Avaliações**: clientes avaliam prestadores com nota e comentário após o evento
- **Favoritos**: clientes salvam prestadores e categorias favoritas
- **Gestão de mídias**: upload de fotos e vídeos para AWS S3 com geração automática de thumbnails via Thumbnailator
- **Observabilidade**: rastreamento distribuído com OpenTelemetry + Jaeger e monitoramento de saúde via Spring Boot Actuator

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.4 |
| Banco de dados | PostgreSQL 17 |
| Migrations | Liquibase |
| ORM | Spring Data JPA / Hibernate |
| Mapeamento de DTOs | MapStruct |
| Autenticação | Spring Security + JWT (Auth0) + OAuth2 Google |
| Geocoding | Nominatim (OpenStreetMap) |
| Armazenamento de mídias | AWS S3 + Thumbnailator |
| E-mail (dev) | Mailtrap |
| Rastreamento | OpenTelemetry Java Agent + Jaeger |
| Documentação da API | SpringDoc OpenAPI (Swagger UI) |
| Containerização | Docker + Docker Compose |

---

## Executando localmente

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker e Docker Compose

### Configuração

Crie um arquivo `.env` na raiz do projeto com as variáveis abaixo:

```env
DB_USER=postgres
DB_PASSWORD=sua_senha
DB_NAME=partydb
DB_URL=jdbc:postgresql://postgres_db:5432/partydb
DB_USERNAME=postgres

ACCESS_KEY_S3=...
SECRET_KEY_S3=...
BUCKET_NAME_S3=...
BUCKET_REGION_S3=sa-east-1

SECRET_KEY_TOKEN=seu_jwt_secret

GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
```

### Subindo apenas a infraestrutura

```bash
docker compose up postgres_db jaeger -d
```

Em seguida, execute a aplicação com o perfil `dev` (padrão):

```bash
./mvnw spring-boot:run
```

O perfil `dev` conecta ao PostgreSQL em `postgres_db:5432`, exibe SQL no console e usa o Mailtrap para e-mails.

### Subindo tudo via Docker

Para rodar a stack completa (infraestrutura + API containerizada):

```bash
docker compose up -d
```

A imagem da API é construída localmente pelo Dockerfile multi-stage.

---

## Serviços

| Serviço | URL | Descrição |
|---|---|---|
| API | http://localhost:8080 | Aplicação principal |
| Swagger UI | http://localhost:8080/swagger-ui.html | Documentação interativa (somente dev) |
| Jaeger UI | http://localhost:16686 | Inspeção visual de traces distribuídos |

---

## Estrutura do projeto

```
src/main/java/com/gabriel/party/
├── controllers/        # Endpoints REST
├── services/           # Regras de negócio
├── repositories/       # Spring Data JPA
├── model/              # Entidades JPA
│   ├── usuario/
│   ├── cliente/
│   ├── prestador/
│   ├── itemcatalogo/   # Produto, Servico e Local (herança JOINED)
│   ├── pedido/
│   ├── avaliacao/
│   └── midia/
├── dtos/               # DTOs de request e response
├── mapper/             # Mappers MapStruct
└── config/             # Security, AWS S3, OpenAPI, etc.

src/main/resources/liquibase/
└── changelog-0.1.0.xml # Schema completo do banco de dados
```

---

## Segurança

- Tokens JWT stateless validados a cada requisição via Security Filter Chain
- Senhas armazenadas com hash BCrypt
- Roles: `ROLE_CLIENTE`, `ROLE_PRESTADOR`, `ROLE_ADMINISTRADOR`
- Login com Google OAuth2 cria o usuário automaticamente no primeiro acesso
- Swagger UI desabilitado no perfil `prod`

---

## Observabilidade

O rastreamento distribuído é habilitado via OpenTelemetry Java Agent, injetado na inicialização do container. Os traces são enviados ao Jaeger via OTLP HTTP na porta `4318` e podem ser inspecionados em `http://localhost:16686`.

A saúde da aplicação é exposta pelo Spring Boot Actuator em `/actuator/health`.
