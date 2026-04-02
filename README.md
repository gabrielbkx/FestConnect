# 🚀 API de Marketplace Hiperlocal para Eventos

Uma API RESTful desenvolvida para conectar organizadores de eventos a prestadores de serviços locais (confeitarias, garçons, animadores, espaços) utilizando buscas geoespaciais baseadas em raio de proximidade.

---

## 📌 Sobre o Projeto

O principal problema na organização de festas e eventos é a logística de encontrar fornecedores qualificados nas proximidades. Esta API resolve essa dor atuando como o motor de um marketplace hiperlocal. Através do uso de coordenadas geográficas (Latitude e Longitude), o sistema permite que um cliente encontre rapidamente os prestadores de serviço mais próximos ao local do evento, otimizando custos de frete e facilitando o contato direto.

### Principais Funcionalidades (MVP)

- **Catálogo de Serviços:** Classificação estruturada de prestadores por categoria.
- **Motor de Busca Geoespacial:** Pesquisa de prestadores dentro de um raio específico em quilômetros, utilizando PostGIS.
- **Gestão de Perfis:** Armazenamento de dados de contato e localização exata de cada negócio.
- **Migrações de Banco de Dados:** Controle de versão de esquema de dados seguro e automatizado.
- **Autenticação e Autorização:** Controle de acesso baseado em roles via Spring Security com JWT.
- **Observabilidade Completa:** Monitoramento de métricas, rastreamento distribuído e visualização em tempo real.

---

## 🛠️ Tecnologias Utilizadas

A arquitetura do projeto foi desenhada no padrão MVC (Model-View-Controller), focando em manutenibilidade e escalabilidade para a nuvem.

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot (Web, Data JPA, Security) |
| Banco de Dados | PostgreSQL + PostGIS |
| Versionamento de Banco | Liquibase |
| Containerização | Docker & Docker Compose |
| Autenticação | Spring Security + JWT |
| Métricas | Micrometer + Prometheus |
| Rastreamento | OpenTelemetry + Jaeger |
| Visualização | Grafana |
| Deploy (Planejado) | — |
| Armazxenamento de Midias| AWS(S3)|

---

## 🔐 Segurança

A API utiliza **Spring Security** para controle de autenticação e autorização:

- Autenticação via **JWT (JSON Web Token)** — tokens stateless gerados no login e validados em cada requisição.
- Controle de acesso baseado em **roles** (ex: `ROLE_CLIENTE`, `ROLE_PRESTADOR`).
- Endpoints públicos e protegidos configurados via Security Filter Chain.
- Senhas armazenadas com hash **BCrypt**.

---

## 📡 Observabilidade

O projeto conta com uma stack completa de observabilidade, permitindo monitorar a saúde da aplicação, rastrear requisições distribuídas e visualizar métricas em tempo real.

### Stack

```
Spring Boot Actuator
       │
       ├── Micrometer ──► Prometheus ──► Grafana (dashboards de métricas)
       │
       └── OpenTelemetry ──► Jaeger (rastreamento distribuído de traces)
```

### Métricas (Prometheus + Grafana)

- Coleta automática de métricas via **Spring Boot Actuator** exposto em `/actuator/prometheus`.
- **Prometheus** realiza scraping periódico do endpoint de métricas.
- **Grafana** exibe dashboards com uptime, uso de heap/non-heap, CPU, requisições HTTP, pool de conexões HikariCP e muito mais.

### Rastreamento Distribuído (OpenTelemetry + Jaeger)

- Instrumentação automática via **OpenTelemetry** para captura de spans por requisição.
- Traces enviados ao **Jaeger** via protocolo OTLP (gRPC na porta `4317`).
- Interface do Jaeger disponível em `http://localhost:16686` para inspeção visual de traces end-to-end.

### Serviços e Portas

| Serviço | Porta | Descrição |
|---|---|---|
| API | `8080` | Aplicação principal |
| Prometheus | `9090` | Coleta de métricas |
| Grafana | `3000` | Dashboards de monitoramento |
| Jaeger UI | `16686` | Visualização de traces |
| Jaeger OTLP gRPC | `4317` | Recepção de traces |
| Jaeger OTLP HTTP | `4318` | Recepção de traces (HTTP) |

---

## ⚙️ Como Executar o Projeto Localmente

### Pré-requisitos

- Java 21+ instalado
- Maven instalado
- Docker e Docker Compose instalados

### Subindo a infraestrutura

```bash
docker compose up -d
```

Isso irá inicializar o  Prometheus, Grafana e Jaeger automaticamente.

### Executando a aplicação

```bash
./mvnw spring-boot:run
```

### Acessando os serviços

| Serviço | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |
| Jaeger UI | http://localhost:16686 |
