# Locks pessimistas no pedido, numa ordem fixa

As corridas que mexem em reserva usam `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`), não `@Version`. O caso de conflito é raro, e quem chega em segundo deve **esperar e reler** o estado (e receber um 400 de domínio), não receber um erro genérico de concorrência. Para não haver deadlock, todo caminho trava na mesma ordem: **o pedido disputado → o prestador → os pedidos PENDENTE/ORCADO**. Por isso o cancelamento do evento trava em duas etapas (primeiro AGUARDANDO_ENTRADA/RESERVADO, depois PENDENTE/ORCADO), e não com uma única busca ordenada por id, que fecharia um ciclo com `fecharConcorrentes` (#104, #105).

## Consequências

Os caminhos que ainda leem sem lock (job de expiração e lembretes, aceite e envio de orçamento) e os deadlocks residuais que hoje viram 500 estão nas issues #108 e #111.
