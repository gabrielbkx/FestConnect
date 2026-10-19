# FestConnect (Resenha): backend

API Spring Boot 3 (Java, JPA/Hibernate, Liquibase, PostgreSQL) do marketplace de serviços para festas. O frontend React fica em outro repositório.

## Agent skills

### Issue tracker

As issues ficam nas GitHub Issues de `gabrielbkx/FestConnect`, operadas pelo `gh` CLI. Veja `docs/agents/issue-tracker.md`.

### Triage labels

Os cinco papéis canônicos usam os labels padrão (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). Veja `docs/agents/triage-labels.md`.

### Domain docs

Layout single-context: `GLOSSARY.md` na raiz e ADRs em `docs/adr/`. Veja `docs/agents/domain.md`.

## Padrões de código

### Comunicação e git

- Escreva issues, PRs, reviews, comentários de código e mensagens de erro em **português do Brasil**. Identificadores podem misturar idiomas como o código já faz.
- Commits: só o subject, curto, no formato `tipo: descrição (#issue)` (`feat`, `fix`, `test`, `chore`, `docs`). Sem corpo longo e **sem** linha `Co-Authored-By`.
- PRs: use a palavra-chave em inglês para fechar issues (`Closes #N`). Merge por squash, só sem achados críticos no review.

### Camadas

- Pacotes por camada e por domínio: `controllers/<dominio>`, `services/<dominio>`, `repositories/<dominio>`, `model/<dominio>`, `dtos/<dominio>`, `mapper/<dominio>`.
- A regra de negócio fica no service. O controller só recebe, valida (`@Valid`), resolve o usuário (`@AuthenticationPrincipal Usuario`) e delega.
- Autorização por papel com `@PreAuthorize("hasAnyRole('ROLE_CLIENTE', ...)")`. A posse do recurso ("este pedido é deste usuário?") é checada no service e devolve 403 `PEDIDO_SEM_PERMISSAO` (ou equivalente) **antes** de revelar qualquer estado.
- DTOs são `record`s com `@Schema` (descrição + exemplo) e Bean Validation com mensagem em PT-BR.
- Todo endpoint documenta o Swagger: `@Operation` + `@ApiResponses` com cada status possível.
- Mapeamento com MapStruct. Campo novo de entidade que não vem do DTO precisa de `@Mapping(target = "...", ignore = true)`.

### Erros

- Erro de domínio é `throw new AppException(ErrorCode.X, args...)`. Cada `ErrorCode` carrega código em snake_case, `HttpStatus` e mensagem em PT-BR com placeholders `%nome%`. Não lance `RuntimeException` crua nem crie exceções por caso.

### Estado do pedido

- As transições permitidas ficam **só** em `StatusPedido.TRANSICOES`. Antes de mudar o status, cheque `podeIrPara`. Os status terminais não aparecem no mapa.
- Concorrência em pedido e reserva usa lock pessimista (`@Lock(PESSIMISTIC_WRITE)` + `@Query`, nos métodos `buscarComLock`/`buscar...ComLock`) e segue a ordem de travamento da `docs/adr/0004-ordem-dos-locks-de-pedido.md`.

### Efeitos colaterais

- E-mail sai sempre por `emailService.enviarAposCommit(...)`, nunca dentro da transação. Os templates ficam em `EmailTemplates` e usam `construir`/`linha`/`caixa`.
- Jobs `@Scheduled` precisam ser idempotentes: filtram por status ou por flag persistida, para que rodar de novo não repita nada.
- Prazos e parâmetros de negócio vêm de `application.properties` (`festconnect.*`) via `@Value`, sem números mágicos.

### Banco

- Toda mudança de schema é um `changelog-X.Y.Z.xml` novo em `src/main/resources/liquibase/`, incluído no fim de `base-changelog.xml`, com `remarks` em cada coluna e o número da issue no comentário. Nunca edite um changeset já aplicado. A versão segue o maior número existente.
- Colunas booleanas novas: `NOT NULL` com `defaultValueBoolean`.

### Testes

- Testes unitários com JUnit 5 + Mockito, espelhando o pacote do service (`src/test/java/.../services/<dominio>/<Classe>Test.java`). Nomeie no formato `metodo_cenario_resultado`.
- Teste a regra de negócio pelo service (status, permissão, prazo, e-mail enviado ou não). `PartyApplicationTests.contextLoads` precisa do Postgres no ar.
- `mvn test` verde antes de abrir a PR.
