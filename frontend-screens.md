claude# Party — Mapeamento de Telas do Frontend

Base URL da API: `http://localhost:8080/api/v1`

Autenticação: `Authorization: Bearer {token}` em todos os endpoints protegidos.

---

## Índice

- [Público](#público)
  - [Home / Landing](#1-home--landing)
  - [Listagem de Prestadores](#2-listagem-de-prestadores)
  - [Perfil do Prestador](#3-perfil-do-prestador)
  - [Detalhe do Item do Catálogo](#4-detalhe-do-item-do-catálogo)
  - [Login](#5-login)
  - [Cadastro](#6-cadastro)
- [Cliente](#cliente)
  - [Meus Pedidos](#7-meus-pedidos)
  - [Detalhe do Pedido (Cliente)](#8-detalhe-do-pedido-cliente)
  - [Solicitar Orçamento](#9-solicitar-orçamento)
  - [Favoritos](#10-favoritos)
  - [Perfil do Cliente](#11-perfil-do-cliente)
- [Prestador](#prestador)
  - [Dashboard](#12-dashboard)
  - [Meu Catálogo](#13-meu-catálogo)
  - [Criar / Editar Item do Catálogo](#14-criar--editar-item-do-catálogo)
  - [Pedidos Recebidos](#15-pedidos-recebidos)
  - [Detalhe do Pedido (Prestador)](#16-detalhe-do-pedido-prestador)
  - [Minhas Avaliações](#17-minhas-avaliações)
  - [Upload de Mídias](#18-upload-de-mídias)
  - [Perfil do Prestador (Edição)](#19-perfil-do-prestador-edição)
- [Admin](#admin)
  - [Gestão de Categorias](#20-gestão-de-categorias)
  - [Gestão de Usuários](#21-gestão-de-usuários)

---

## Público

### 1. Home / Landing

**Acesso:** Qualquer usuário (sem login)

**Dados a exibir:**
- Lista de categorias disponíveis (nome + ícone)
- Barra de busca com campo de texto e localização (lat/lon via Geolocation API do browser)

**Ações:**
- Selecionar categoria → vai para Listagem de Prestadores com filtro de categoria
- Buscar por texto → vai para Listagem de Prestadores com filtro de busca
- Clicar em "Entrar" → Login
- Clicar em "Cadastrar" → Cadastro

**Endpoints:**
```
GET /categorias?size=20
```

**Resposta de categorias:**
```json
{
  "content": [
    {
      "id": "uuid",
      "nome": "DJ",
      "descricao": "...",
      "iconeUrl": "https://...",
      "quantidadePrestadores": 12,
      "ativo": true
    }
  ]
}
```

---

### 2. Listagem de Prestadores

**Acesso:** Qualquer usuário (sem login)

**Dados a exibir (card por prestador):**
- Foto de perfil (`fotoPerfilUrl`)
- Nome (`nome`)
- Categoria (`categoriaNome`)
- Cidade e estado (`cidade`, `estado`)
- Nota média com estrelas (`mediaAvaliacoes`) + quantidade (`quantidadeAvaliacoes`)

**Filtros disponíveis:**
- Por categoria + raio: usa `/prestadores/filtro-radar`
- Por proximidade sem categoria: usa `/prestadores/proximidade`
- Paginação padrão: usa `/prestadores`

**Ações:**
- Clicar no card → Perfil do Prestador
- Alterar filtros → recarrega lista

**Endpoints:**
```
GET /prestadores?page=0&size=10&sort=nomeCompleto
GET /prestadores/proximidade?lat={lat}&lon={lon}&raio=10
GET /prestadores/filtro-radar?categoriaId={id}&lat={lat}&lon={lon}&raio=50
```

**Resposta (`PrestadorResumoDTO`):**
```json
{
  "id": "uuid",
  "nome": "DJ Marcos",
  "fotoPerfilUrl": "https://...",
  "descricao": "DJ com 10 anos de experiência",
  "categoriaNome": "DJ",
  "cidade": "São Paulo",
  "estado": "SP",
  "mediaAvaliacoes": 4.7,
  "quantidadeAvaliacoes": 23
}
```

---

### 3. Perfil do Prestador

**Acesso:** Qualquer usuário (sem login)

**Dados a exibir:**
- Foto de perfil
- Nome, categoria, cidade/estado
- Descrição completa
- WhatsApp (link `https://wa.me/{numero}`)
- Nota média + quantidade de avaliações
- Galeria de fotos/vídeos do prestador
- Lista de itens do catálogo (Produto / Serviço / Local) com título, preço base e foto
- Lista de avaliações (nota, comentário, nome do cliente, data)

**Ações:**
- Clicar em item do catálogo → Detalhe do Item
- Clicar em "Solicitar Orçamento" → Solicitar Orçamento (requer login de cliente)
- Clicar em WhatsApp → abre WhatsApp
- Clicar em "Favoritar" → adiciona aos favoritos (requer login de cliente)

**Endpoints:**
```
GET /prestadores/{id}
GET /midias/{prestadorId}/prestador?tipoMidia=IMAGEM
GET /avaliacoes/prestador/{prestadorId}?page=0&size=5
```

**Resposta (`PrestadorResponseDTO`):**
```json
{
  "id": "uuid",
  "nome": "DJ Marcos",
  "email": "dj@email.com",
  "whatsapp": "11999999999",
  "categoriaId": "uuid",
  "categoriaNome": "DJ",
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": 100,
    "bairro": "Centro",
    "cidade": "São Paulo",
    "estado": "SP",
    "cep": "01001000",
    "latitude": -23.55,
    "longitude": -46.63
  },
  "itensCatalogo": [ /* ItemCatalogoResponseDTO[] */ ],
  "midias": [ /* MidiaResponseDTO[] */ ],
  "avaliacoes": [ /* AvaliacaoResponseDTO[] */ ],
  "mediaAvaliacoes": 4.7,
  "quantidadeAvaliacoes": 23
}
```

---

### 4. Detalhe do Item do Catálogo

**Acesso:** Qualquer usuário (sem login)

**Dados a exibir:**
- Título, descrição, preço base
- Tipo (PRODUTO / SERVIÇO / LOCAL)
- Galeria de mídias do item
- **Se LOCAL:** capacidade máxima, metragem, permite som (sim/não), tem estacionamento (sim/não), tipo de espaço
- Nome do prestador (com link para o perfil)

**Ações:**
- Clicar em "Ver Prestador" → Perfil do Prestador
- Clicar em "Solicitar Orçamento" → Solicitar Orçamento (requer login de cliente)

**Endpoints:**
```
GET /itens-catalogo/{id}
```

**Resposta (`ItemCatalogoResponseDTO`):**
```json
{
  "id": "uuid",
  "titulo": "Churrasqueira para 100 pessoas",
  "descricao": "...",
  "precoBase": 1500.00,
  "tipo": "LOCAL",
  "ativo": true,
  "midias": [ /* MidiaResponseDTO[] */ ],
  "localDetalhe": {
    "capacidadeMaxima": 100,
    "metragem": 200.0,
    "permiteSom": true,
    "temEstacionamento": false,
    "tipoEspaco": "Área externa coberta"
  }
}
```

---

### 5. Login

**Acesso:** Qualquer usuário

**Dados do formulário:**
- Email
- Senha
- Link "Esqueci minha senha"

**Ações:**
- Submeter → chama `/auth/login`, armazena token JWT no localStorage/cookie
- "Esqueci minha senha" → fluxo de recuperação (3 passos: email → código PIN → nova senha)

**Endpoints:**
```
POST /auth/login
Body: { "email": "...", "senha": "..." }

POST /auth/recuperacao-senha
Body: { "email": "..." }

POST /auth/validar-codigo
Body: { "email": "...", "codigoPin": "123456" }
→ retorna token de recuperação

POST /auth/redefinir-senha
Body: { "token": "...", "novaSenha": "..." }
```

**Resposta do login (`TokenResponseDTO`):**
```json
{ "token": "eyJ..." }
```

> Após o login, decodificar o JWT para extrair o role (`ROLE_CLIENTE`, `ROLE_PRESTADOR`, `ROLE_ADMINISTRADOR`) e redirecionar adequadamente.

---

### 6. Cadastro

**Acesso:** Qualquer usuário

**Dois fluxos distintos — escolha na entrada:**

**Cadastro de Cliente:**
- Nome completo
- Email
- Senha
- Endereço (opcional)

**Cadastro de Prestador:**
- Nome completo / razão social
- Email
- Senha
- WhatsApp (obrigatório)
- CPF ou CNPJ (obrigatório)
- Categoria (select com lista de categorias) — obrigatório
- Endereço (opcional)

**Endpoints:**
```
GET /categorias?size=50          ← para popular o select de categorias

POST /auth/cadastro/cliente
Body: {
  "nomeCompleto": "...",
  "email": "...",
  "senha": "...",
  "endereco": { ... }            ← opcional
}

POST /auth/cadastro/prestador
Body: {
  "nomeCompleto": "...",
  "email": "...",
  "senha": "...",
  "whatsapp": "11999999999",
  "cnpjOuCpf": "12345678901",
  "categoriaId": "uuid",
  "endereco": { ... }            ← opcional
}
```

**Resposta (`CadastroResponseDTO`):**
```json
{
  "id": "uuid",
  "nomeCompleto": "...",
  "email": "...",
  "token": "eyJ..."
}
```

---

## Cliente

> Todas as telas abaixo exigem `Authorization: Bearer {token}` com role `ROLE_CLIENTE`.

---

### 7. Meus Pedidos

**Acesso:** Cliente autenticado

**Dados a exibir (lista de cards):**
- Nome do prestador
- Data do evento
- Tipo do evento
- Status com badge colorido:
  - `PENDENTE` → aguardando orçamento
  - `ORCADO` → orçamento recebido (destaque — requer ação)
  - `ACEITO` → confirmado
  - `RECUSADO` → recusado pelo prestador
  - `CANCELADO` → cancelado pelo cliente
  - `EXPIRADO` → orçamento vencido

**Ações:**
- Clicar no card → Detalhe do Pedido (Cliente)

**Endpoints:**
```
GET /pedidos/cliente
```

**Resposta (lista de `PedidoResponseDTO`):**
```json
[
  {
    "id": "uuid",
    "nomeCliente": "Ana",
    "fotoClienteUrl": "https://...",
    "nomePrestador": "DJ Marcos",
    "dataEvento": "2026-06-15T18:00:00",
    "localEvento": "Rua das Flores, 100",
    "tipoEvento": "Festa de aniversário",
    "numeroConvidados": 50,
    "descricao": "...",
    "status": "ORCADO",
    "valor": 800.00,
    "detalhesOrcamento": "Inclui equipamentos...",
    "validadeOrcamento": "2026-05-20T23:59:59",
    "dataHoraCriacao": "2026-05-10T10:00:00"
  }
]
```

---

### 8. Detalhe do Pedido (Cliente)

**Acesso:** Cliente autenticado

**Dados a exibir:**
- Todos os campos do pedido (evento, local, tipo, nº convidados, descrição)
- Nome e foto do prestador (link para perfil)
- Status atual
- **Se status = ORCADO:**
  - Valor do orçamento
  - Detalhes do orçamento
  - Validade do orçamento (exibir countdown ou data)
  - Botões: **Aceitar** e **Recusar**
- **Se status = ACEITO:**
  - WhatsApp do prestador para contato
  - Botão: **Avaliar Prestador** (se ainda não avaliou)

**Ações:**
- Aceitar orçamento → `PUT /pedidos/{id}/aceitar`
- Cancelar pedido → `PUT /pedidos/{id}/cancelar`
- Avaliar prestador → abre formulário de avaliação → `POST /avaliacoes`

**Endpoints:**
```
PUT /pedidos/{id}/aceitar
PUT /pedidos/{id}/cancelar

POST /avaliacoes
Body: {
  "nota": 5,
  "comentario": "Ótimo serviço!",
  "prestadorId": "uuid"
}
```

---

### 9. Solicitar Orçamento

**Acesso:** Cliente autenticado

**Contexto:** Chegou via Perfil do Prestador — `prestadorId` já é conhecido.

**Formulário:**
- Data e horário do evento (datetime picker, deve ser futuro)
- Local do evento (texto livre)
- Tipo do evento (ex: aniversário, casamento, corporativo...)
- Número de convidados (inteiro ≥ 1)
- Descrição / observações (textarea)

**Ações:**
- Submeter → cria pedido, redireciona para Meus Pedidos

**Endpoints:**
```
POST /pedidos
Body: {
  "prestadorId": "uuid",
  "dataEvento": "2026-06-15T18:00:00",
  "localEvento": "Rua das Flores, 100, São Paulo",
  "tipoEvento": "Aniversário",
  "numeroConvidados": 50,
  "descricao": "Preciso de DJ das 18h às 23h..."
}
```

---

### 10. Favoritos

**Acesso:** Cliente autenticado

> **Nota:** O campo `favoritePrestadores` existe no modelo de dados, mas não há endpoint dedicado para adicionar/remover favoritos mapeado nos controllers atuais. Pode ser que precise implementar esse endpoint no backend ou usar solução local (localStorage) no MVP.

**Dados a exibir:**
- Lista de prestadores favoritados (mesmo card da Listagem de Prestadores)

**Ações:**
- Clicar no card → Perfil do Prestador
- Remover dos favoritos

---

### 11. Perfil do Cliente

**Acesso:** Cliente autenticado

**Dados a exibir / editar:**
- Nome completo
- Email
- WhatsApp
- Endereço (CEP, logradouro, número, complemento, bairro, cidade, estado)

**Ações:**
- Salvar alterações → `PUT /clientes/{id}`

**Endpoints:**
```
GET /clientes/{id}              ← para carregar dados atuais

PUT /clientes/{id}
Body: {
  "nome": "...",
  "email": "...",
  "whatsapp": "11999999999",
  "endereco": {
    "cep": "01001000",
    "logradouro": "Rua das Flores",
    "numero": 100,
    "complemento": "Apto 12",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "estado": "SP"
  }
}
```

---

## Prestador

> Todas as telas abaixo exigem `Authorization: Bearer {token}` com role `ROLE_PRESTADOR`.

---

### 12. Dashboard

**Acesso:** Prestador autenticado

**Dados a exibir:**
- Quantidade de pedidos pendentes (badge de notificação)
- Lista resumida dos pedidos mais recentes com status
- Nota média e quantidade de avaliações
- Atalhos: Meu Catálogo, Pedidos, Avaliações

**Endpoints:**
```
GET /pedidos/prestador/pendentes    ← contar e listar pendentes
GET /pedidos/prestador/historico    ← historico completo para resumo
```

---

### 13. Meu Catálogo

**Acesso:** Prestador autenticado

**Dados a exibir (lista de cards):**
- Título do item
- Tipo (badge: PRODUTO / SERVIÇO / LOCAL)
- Preço base
- Status (ativo/inativo)
- Primeira mídia do item como thumbnail

**Ações:**
- Criar novo item → Criar / Editar Item
- Clicar no card → Criar / Editar Item (modo edição)
- Desativar item → `DELETE /itens-catalogo/{id}`

**Endpoints:**
```
GET /itens-catalogo/prestador/{prestadorId}
DELETE /itens-catalogo/{id}
```

---

### 14. Criar / Editar Item do Catálogo

**Acesso:** Prestador autenticado

**Formulário base (todos os tipos):**
- Título (obrigatório)
- Descrição
- Preço base (R$)
- Tipo: PRODUTO / SERVIÇO / LOCAL (select — apenas na criação; não pode alterar tipo após criado)

**Campos extras se tipo = LOCAL:**
- Capacidade máxima (inteiro)
- Metragem (m²)
- Permite som? (toggle sim/não)
- Tem estacionamento? (toggle sim/não)
- Tipo de espaço (texto livre — ex: "Área externa coberta")

**Ações:**
- Salvar → `POST` (criação) ou `PUT` (edição)
- Após salvar, redirecionar para Upload de Mídias do item

**Endpoints:**
```
POST /itens-catalogo
Body: {
  "titulo": "...",
  "descricao": "...",
  "precoBase": 500.00,
  "tipo": "LOCAL",
  "localDetalhe": {                 ← apenas se tipo = LOCAL
    "capacidadeMaxima": 80,
    "metragem": 150.0,
    "permiteSom": true,
    "temEstacionamento": true,
    "tipoEspaco": "Salão fechado"
  }
}

PUT /itens-catalogo/{id}
Body: { ... mesmo shape ... }
```

---

### 15. Pedidos Recebidos

**Acesso:** Prestador autenticado

**Duas abas:**
- **Pendentes:** pedidos aguardando orçamento → `GET /pedidos/prestador/pendentes`
- **Histórico:** todos os pedidos → `GET /pedidos/prestador/historico`

**Dados a exibir por card:**
- Nome e foto do cliente
- Data do evento
- Tipo do evento
- Número de convidados
- Status com badge colorido

**Ações:**
- Clicar no card → Detalhe do Pedido (Prestador)

**Endpoints:**
```
GET /pedidos/prestador/pendentes
GET /pedidos/prestador/historico
```

---

### 16. Detalhe do Pedido (Prestador)

**Acesso:** Prestador autenticado

**Dados a exibir:**
- Foto e nome do cliente
- Data do evento, local, tipo, nº de convidados
- Descrição/observações do cliente
- Status atual

**Se status = PENDENTE — formulário para enviar orçamento:**
- Valor (R$) — obrigatório
- Detalhes do orçamento (textarea) — obrigatório
- Validade do orçamento (datetime picker, deve ser futuro) — obrigatório
- Botões: **Enviar Orçamento** e **Recusar Pedido**

**Se status = ORCADO ou posterior:**
- Exibir o orçamento enviado (somente leitura)

**Ações:**
- Enviar orçamento → `PUT /pedidos/{id}/orcar`
- Recusar pedido → `PUT /pedidos/{id}/recusar`

**Endpoints:**
```
PUT /pedidos/{id}/orcar
Body: {
  "valor": 800.00,
  "detalhesOrcamento": "Inclui equipamento completo...",
  "validadeOrcamento": "2026-05-20T23:59:59"
}

PUT /pedidos/{id}/recusar
(sem body)
```

---

### 17. Minhas Avaliações

**Acesso:** Prestador autenticado

**Dados a exibir:**
- Nota média geral com estrelas
- Lista de avaliações:
  - Estrelas (1–5)
  - Comentário
  - Nome do cliente
  - Data da avaliação

**Endpoints:**
```
GET /avaliacoes/prestador/{prestadorId}?page=0&size=10&sort=dataCriacao,desc
```

**Resposta (`AvaliacaoResponseDTO`):**
```json
{
  "id": "uuid",
  "nota": 5,
  "comentario": "Excelente serviço!",
  "dataCriacao": "2026-04-20T14:30:00",
  "prestadorId": "uuid",
  "prestadorNome": "DJ Marcos",
  "clienteId": "uuid",
  "clienteNome": "Ana Lima"
}
```

---

### 18. Upload de Mídias

**Acesso:** Prestador autenticado

**Contexto:** Mídias podem ser do perfil geral do prestador ou de um item específico do catálogo.

**Dados a exibir:**
- Galeria atual de fotos/vídeos (ordenada por `ordem`)
- Opção de arrastar para reordenar

**Ações:**
- Upload de nova imagem ou vídeo → `POST /midias` (multipart)
- Reordenar → `PUT /midias/{id}` (atualiza `ordem`)
- Deletar mídia → `DELETE /midias/{id}`

**Endpoints:**
```
GET /midias/{prestadorId}/prestador

POST /midias
Content-Type: multipart/form-data
  arquivo: [File]
  dto: {
    "tipo": "IMAGEM",
    "ordem": 1,
    "itemCatalogoId": "uuid"    ← null se for mídia do perfil
  }

PUT /midias/{id}
Body: {
  "tipo": "IMAGEM",
  "ordem": 2,
  "itemCatalogoId": "uuid"
}

DELETE /midias/{id}
```

---

### 19. Perfil do Prestador (Edição)

**Acesso:** Prestador autenticado

**Dados a exibir / editar:**
- Nome completo / razão social
- Email
- Descrição dos serviços (textarea)
- WhatsApp
- Categoria (select)
- Endereço (CEP, logradouro, número, complemento, bairro, cidade, estado)
- Foto de perfil (via Upload de Mídias)

**Ações:**
- Salvar → `PUT /prestadores/{id}`
- O backend faz geocoding automático do endereço (não enviar lat/lon)

**Endpoints:**
```
GET /categorias?size=50              ← para popular o select

GET /prestadores/{id}                ← para carregar dados atuais

PUT /prestadores/{id}
Body: {
  "nome": "...",
  "email": "...",
  "descricao": "...",
  "whatsapp": "11999999999",
  "categoriaId": "uuid",
  "endereco": {
    "cep": "01001000",
    "logradouro": "Rua das Flores",
    "numero": 100,
    "complemento": "",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "estado": "SP"
  }
}
```

---

## Admin

> Todas as telas abaixo exigem `Authorization: Bearer {token}` com role `ROLE_ADMINISTRADOR`.

---

### 20. Gestão de Categorias

**Acesso:** Admin autenticado

**Dados a exibir:**
- Tabela com: nome, descrição, ícone, quantidade de prestadores, status (ativo/inativo)

**Ações:**
- Criar categoria → formulário inline ou modal → `POST /categorias`
- Editar categoria → `PUT /categorias/{id}`
- Desativar categoria → `DELETE /categorias/{id}`

**Endpoints:**
```
GET /categorias?page=0&size=20

POST /categorias
Body: {
  "nome": "Buffet",
  "descricao": "Serviços de buffet para eventos",
  "iconeUrl": "https://..."
}

PUT /categorias/{id}
Body: { ... mesmo shape ... }

DELETE /categorias/{id}
```

---

### 21. Gestão de Usuários

**Acesso:** Admin autenticado

**Dados a exibir:**
- Duas abas: Clientes / Prestadores
- Tabela com: nome, email, cidade, status (ativo/inativo)

**Ações:**
- Desativar usuário → `DELETE /clientes/{id}` ou `DELETE /prestadores/{id}`
- Ver perfil do prestador → Perfil do Prestador (leitura)

**Endpoints:**
```
GET /clientes?page=0&size=20
GET /prestadores?page=0&size=20

DELETE /clientes/{id}
DELETE /prestadores/{id}
```

---

## Observações Gerais

### Autenticação e Redirecionamento
- Após login, decodificar o JWT (campo `role`) para redirecionar:
  - `ROLE_CLIENTE` → Meus Pedidos
  - `ROLE_PRESTADOR` → Dashboard
  - `ROLE_ADMINISTRADOR` → Gestão de Categorias
- Rotas protegidas devem redirecionar para Login se não houver token válido

### Paginação
- Todos os endpoints paginados retornam o shape padrão do Spring:
```json
{
  "content": [ ... ],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10
}
```

### EnderecoDTO (shape compartilhado)
```json
{
  "logradouro": "Rua das Flores",
  "numero": 100,
  "complemento": "Apto 12",
  "bairro": "Centro",
  "cep": "01001000",
  "cidade": "São Paulo",
  "estado": "SP"
}
```
> Não enviar `latitude` e `longitude` — o backend preenche automaticamente via geocoding.

### Pendência: Favoritos
O modelo de dados tem suporte a favoritos (`favoritePrestadores` no Cliente), mas não há endpoints REST implementados para isso. No MVP, avaliar se usa localStorage ou implementa o endpoint no backend.

### Flow de Status dos Pedidos
```
PENDENTE → (prestador envia orçamento) → ORCADO
ORCADO   → (cliente aceita)            → ACEITO
ORCADO   → (validade vence)            → EXPIRADO
PENDENTE → (prestador recusa)          → RECUSADO
PENDENTE/ORCADO → (cliente cancela)   → CANCELADO
```