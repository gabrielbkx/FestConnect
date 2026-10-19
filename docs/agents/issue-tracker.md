# Issue tracker: GitHub

As issues e specs deste repo ficam nas GitHub Issues de `gabrielbkx/FestConnect`. Use o `gh` CLI para todas as operações. Escreva títulos e corpos em português do Brasil.

## Convenções

- **Criar uma issue**: `gh issue create --title "..." --body "..."`. Use heredoc para corpos com várias linhas.
- **Ler uma issue**: `gh issue view <número> --json number,title,body,labels,comments`.
- **Listar issues**: `gh issue list --state open --json number,title,body,labels,comments --jq '[.[] | {number, title, body, labels: [.labels[].name], comments: [.comments[].body]}]'`, com os filtros `--label` e `--state` adequados.
- **Tornar uma issue sub-issue de uma pai**: `gh issue create --parent <pai> ...`, ou depois com `gh issue edit <pai> --add-sub-issue <filha>` (`gh` 2.94+). Em `gh` mais antigo: `gh api --method POST repos/<owner>/<repo>/issues/<pai>/sub_issues -F sub_issue_id=<id-de-banco-da-filha>` (id de banco, como em **Bloqueio** abaixo). Sem sub-issues, ponha `Part of #<pai>` no topo do corpo da filha.
- **Comentar numa issue**: `gh issue comment <número> --body "..."`
- **Aplicar / remover labels**: `gh issue edit <número> --add-label "..."` / `--remove-label "..."`
- **Fechar**: `gh issue close <número> --comment "..."`
- **Fechar via PR**: no corpo da PR, use a palavra-chave em inglês (`Closes #N`). "Fecha #N" não fecha a issue.

O `gh` deduz o repo pelo `git remote -v` quando roda dentro de um clone.

## Pull requests como superfície de triagem

**PRs como canal de pedidos: no.** _(Mude para `yes` se o repo passar a tratar PRs externas como pedidos de feature. O `/triage` lê este flag.)_

Com `yes`, as PRs passam pelos mesmos labels e estados das issues, usando os equivalentes de `gh pr`:

- **Ler uma PR**: `gh pr view <número> --comments` e, para o diff, `gh pr diff <número>`.
- **Listar PRs externas para triagem**: `gh api --paginate 'repos/{owner}/{repo}/pulls?state=open' --jq '.[] | select(.author_association | IN("OWNER","MEMBER","COLLABORATOR") | not) | {number, title, author: .user.login, author_association, labels: [.labels[].name]}'`.
- **Comentar / rotular / fechar**: `gh pr comment`, `gh pr edit --add-label`/`--remove-label`, `gh pr close`.

O GitHub usa a mesma numeração para issues e PRs, então um `#42` solto pode ser qualquer um dos dois: tente `gh pr view 42` e, se não for PR, `gh issue view 42`.

## Quando uma skill disser "publicar no issue tracker"

Crie uma GitHub issue.

## Quando uma skill disser "buscar o ticket relevante"

Leia-o como em **Ler uma issue** acima.

## Operações de wayfinding

Usadas pelo `/wayfinder`. O **mapa** é uma única issue, e os tickets são issues **filhas** dela.

- **Mapa**: uma issue com o label `wayfinder:map`, cujo corpo tem as seções Notes / Decisions-so-far / Fog. `gh issue create --label wayfinder:map`.
- **Ticket filho**: uma issue ligada ao mapa como GitHub sub-issue (veja **Tornar uma issue sub-issue**). Onde sub-issues não estiverem habilitadas, adicione a filha a uma task list no corpo do mapa e ponha `Part of #<mapa>` no topo do corpo dela. Labels: `wayfinder:<tipo>` (`research`/`prototype`/`grilling`/`task`). Ao ser assumido, o ticket fica atribuído a quem está conduzindo.
- **Bloqueio**: as **dependências nativas de issue** do GitHub, que são a representação canônica e aparecem na UI. Para criar uma aresta: `gh api --method POST repos/<owner>/<repo>/issues/<filha>/dependencies/blocked_by -F issue_id=<id-de-banco-do-bloqueador>`. O `<id-de-banco-do-bloqueador>` é o **database id** numérico (`gh api repos/<owner>/<repo>/issues/<n> --jq .id`), não o `#número` nem o `node_id`. O GitHub expõe `issue_dependencies_summary.blocked_by`, que conta só bloqueadores abertos e é a trava que vale. Onde não houver dependências, use uma linha `Blocked by: #<n>, #<n>` no topo do corpo da filha. Um ticket está desbloqueado quando todos os bloqueadores estão fechados.
- **Consulta da fronteira**: liste as filhas abertas do mapa (`gh issue list --state open`, restrito às sub-issues / task list do mapa) e descarte as que têm bloqueador aberto (`issue_dependencies_summary.blocked_by > 0`, ou issue aberta na linha `Blocked by`) ou responsável atribuído. Vence a primeira na ordem do mapa.
- **Assumir**: `gh issue edit <n> --add-assignee @me`, que é a primeira escrita da sessão.
- **Resolver**: `gh issue comment <n> --body "<resposta>"`, depois `gh issue close <n>`, depois acrescente um ponteiro (resumo + link) em Decisions-so-far do mapa.
