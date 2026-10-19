# Arras derivadas de quem cancelou; o dinheiro fica fora do monólito

Quando uma reserva é cancelada, o monólito só registra **quem** cancelou (`canceladoPor`) e o motivo. A regra de arras (cliente desiste → perde o sinal; prestador desiste → devolve) é derivada desse campo, e o estorno real será feito pelo microserviço de pagamentos (#102). Mover dinheiro dentro do monólito agora criaria uma lógica de estorno que teria de ser reescrita na extração (#90, opção c).

## Consequências

`canceladoPor` vira dado financeiro: escritas concorrentes nele precisam de lock (ver ADR-0004).
