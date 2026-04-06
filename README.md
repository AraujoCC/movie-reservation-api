# 🎬 Movie Reservation API

API de reserva de filmes construída com arquitetura moderna, utilizando **Spring Boot**, **Event-Driven Architecture** e múltiplos bancos de dados.

---

## 🚀 Sobre o Projeto

A **Movie Reservation API** é um sistema backend para gerenciamento de reservas de filmes, permitindo:

- Cadastro e autenticação de usuários (JWT)
- Criação e gerenciamento de reservas
- Controle de sessões e assentos
- Cancelamento automático de reservas expiradas
- Processamento de eventos com Kafka

O projeto foi desenvolvido com foco em **boas práticas de backend**, **escalabilidade** e **arquitetura baseada em eventos**.

---

## 🧱 Arquitetura

O sistema segue uma abordagem **modular e orientada a eventos**, com integração entre diferentes tecnologias:

- **REST API (Spring Boot)**
- **Event-Driven com Kafka**
- **Persistência híbrida (SQL + NoSQL)**

### 🔄 Fluxo de reserva

1. Usuário cria uma reserva
2. Sistema salva no PostgreSQL
3. Evento é publicado no Kafka
4. Outros serviços podem consumir o evento
5. Reservas expiradas são canceladas automaticamente

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot**
- **Spring Security (JWT)**
- **Spring Data JPA**
- **Spring Data MongoDB**
- **Apache Kafka**
- **PostgreSQL**
- **MongoDB**
- **Flyway (migrations)**
- **Testcontainers (testes de integração)**
- **JUnit 5**

---

## 📦 Estrutura do Projeto


src/
├── controller # Endpoints da API
├── service # Regras de negócio
├── repository # Acesso a dados
├── entity # Entidades JPA
├── dto # Objetos de transferência
├── config # Configurações (Security, Kafka, etc)
└── events # Eventos e mensageria


---

## 🔐 Autenticação

A API utiliza **JWT (JSON Web Token)** para autenticação.

### Fluxo:
1. Usuário se registra
2. Faz login
3. Recebe um token JWT
4. Usa o token nas requisições protegidas

---

## 📊 Banco de Dados

### 🐘 PostgreSQL
- Usuários
- Reservas
- Sessões
- Assentos

### 🍃 MongoDB
- Eventos
- Logs
- Dados não estruturados

---

## ⚡ Mensageria (Kafka)

O sistema utiliza o **Apache Kafka** para comunicação assíncrona.

Exemplo de eventos:
- `reservation-created`
- `reservation-cancelled`

---

## 🧪 Testes

O projeto possui:

### ✅ Testes Unitários
- Focados na lógica de negócio (`Service`)
- Não dependem de banco ou Docker

### 🔄 Testes de Integração
- Utilizam **Testcontainers**
- Sobem containers (PostgreSQL) automaticamente

---

## ▶️ Como Executar o Projeto

### 🔧 Pré-requisitos

- Java 21
- Maven
- Docker (opcional, para testes)
- PostgreSQL
- MongoDB
- Kafka

---

### ▶️ Rodar a aplicação

```bash
./mvnw spring-boot:run
🧪 Rodar testes
./mvnw test

⚠️ Testes de integração requerem Docker ativo

📌 Funcionalidades
 Cadastro de usuários
 Autenticação com JWT
 Criação de reservas
 Cancelamento de reservas
 Expiração automática de reservas
 Integração com Kafka
 Persistência com PostgreSQL
 Uso de MongoDB para eventos
 Testes unitários
📈 Melhorias Futuras
 Deploy com Docker Compose
 Observabilidade (Prometheus + Grafana)
 Rate limiting
 Cache com Redis
 Testes E2E
 Documentação com Swagger/OpenAPI
🤝 Contribuição

Contribuições são bem-vindas!

Fork o projeto
Crie uma branch (feature/minha-feature)
Commit suas mudanças
Push para a branch
Abra um Pull Request
👨‍💻 Autor

Pedro Isaac

💼 Em busca da primeira oportunidade como desenvolvedor backend
🚀 Focado em Java, Spring Boot e arquitetura de microsserviços
⭐ Destaque

Este projeto demonstra:

Arquitetura moderna com eventos (Kafka)
Integração com múltiplos bancos
Boas práticas com Spring Boot
Testes automatizados
Estrutura pronta para escalar

---

## 🔥 Próximo nível (se quiser melhorar mais ainda)

Posso te ajudar a deixar isso ainda mais forte:

- adicionar **badge (build passing, Java, etc)**
- incluir **print de endpoints (Postman/Swagger)**
- escrever uma descrição perfeita pro LinkedIn
- montar um **README estilo projeto de empresa**

---

Se quiser, me manda o link do GitHub que eu:
👉 reviso o README  
👉 ajusto pra nível vaga júnior real  
👉 e deixo ele pronto pra recrutador bater o olho e curtir 🚀
