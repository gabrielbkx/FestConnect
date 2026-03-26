# 🚀 API de Marketplace Hiperlocal para Eventos

Uma API RESTful desenvolvida para conectar organizadores de eventos a prestadores de serviços locais (confeitarias, garçons, animadores, espaços) utilizando buscas geoespaciais baseadas em raio de proximidade.

## 📌 Sobre o Projeto

O principal problema na organização de festas e eventos é a logística de encontrar fornecedores qualificados nas proximidades. Esta API resolve essa dor atuando como o motor de um marketplace hiperlocal. Através do uso de coordenadas geográficas (Latitude e Longitude), o sistema permite que um cliente encontre rapidamente os prestadores de serviço mais próximos ao local do evento, otimizando custos de frete e facilitando o contato direto.

### Principais Funcionalidades (MVP)
* **Catálogo de Serviços:** Classificação estruturada de prestadores por categoria.
* **Motor de Busca Geoespacial:** Pesquisa de prestadores dentro de um raio específico em quilômetros, utilizando PostGIS.
* **Gestão de Perfis:** Armazenamento de dados de contato e localização exata de cada negócio.
* **Migrações de Banco de Dados:** Controle de versão de esquema de dados seguro e automatizado.

## 🛠️ Tecnologias Utilizadas

A arquitetura do projeto foi desenhada no padrão **MVC (Model-View-Controller)**, focando em manutenibilidade e escalabilidade para a nuvem.

* **Linguagem:** Java
* **Framework:** Spring Boot (Spring Web, Spring Data JPA)
* **Banco de Dados:** PostgreSQL
* **Geolocalização:** 
* **Versionamento de Banco:** Liquibase
* **Containerização:** Docker & Docker Compose
* **Deploy (Planejado):** 


## ⚙️ Como Executar o Projeto Localmente

### Pré-requisitos
* Java 21+ instalado
* Maven instalado
* Docker e Docker Compose instalados

