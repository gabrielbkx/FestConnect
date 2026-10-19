# Resenha (FestConnect)

Marketplace que conecta quem organiza uma festa a quem presta serviços para ela, do pedido de orçamento até o evento acontecer.

## Pessoas

**Cliente**:
Quem organiza o evento e contrata serviços.
_Evitar_: contratante, comprador, usuário (usuário é só a conta de login)

**Prestador**:
Quem oferece serviços, produtos ou locais para eventos. "Parceiro" é só o rótulo mostrado ao usuário na tela e nos e-mails.
_Evitar_: parceiro (fora da interface), fornecedor, vendedor

**Administrador**:
Quem modera a plataforma (remove mídias e itens).
_Evitar_: moderador, staff

## Catálogo

**Item de catálogo**:
Algo que um prestador oferece e que pode ser pedido: um serviço, um produto ou um local. Cada item pertence a uma categoria.
_Evitar_: anúncio, produto (produto é só um dos tipos)

**Categoria**:
O tipo de oferta de um item (DJ, buffet, decoração…), agrupado em macroáreas.
_Evitar_: segmento, nicho

## Contratação

**Evento**:
A festa do cliente, usada para agrupar pedidos. É opcional: um pedido pode existir sem evento.
_Evitar_: festa (na linguagem do código), ocasião

**Pedido**:
A solicitação de um cliente por um item de catálogo, para uma data e um intervalo de horário. Passa por orçamento, sinal e reserva até ser concluído ou encerrado.
_Evitar_: contrato, solicitação, booking

**Orçamento**:
O valor que o prestador propõe para um pedido. O cliente aceita ou recusa.
_Evitar_: cotação, proposta

**Sinal**:
O percentual do orçamento que o cliente paga para garantir a data. Cada prestador define seu percentual.
_Evitar_: entrada, adiantamento, depósito

**Reserva**:
O estado do pedido depois que o sinal foi pago: a data passa a ser do cliente e a agenda do prestador fica travada nesse intervalo.
_Evitar_: confirmação, agendamento

**Arras**:
A regra sobre o destino do sinal quando uma reserva é cancelada: se o cliente desiste, perde o sinal; se o prestador desiste, devolve.
_Evitar_: multa, penalidade

**Concorrentes**:
Os pedidos ainda não reservados (em orçamento ou aguardando o sinal) que disputam o mesmo intervalo na agenda do mesmo prestador. Quem paga o sinal primeiro fica com a data; quando a capacidade do horário se esgota, os concorrentes restantes perdem a data na hora.
_Evitar_: conflitantes, duplicados

**Recusa**:
O fim de um pedido por decisão de uma das partes antes da reserva: o prestador não atende ou o cliente rejeita o orçamento.
_Evitar_: rejeição, negativa

**Cancelamento de pedido**:
A desistência do cliente antes da reserva, sem motivo nem prazo, porque nada foi pago.
_Evitar_: cancelamento de reserva, desistência

**Expiração**:
O fim de um pedido porque um prazo de resposta, de aceite ou de pagamento do sinal venceu sem ação.
_Evitar_: vencimento, timeout

**Fechamento**:
O fim de um pedido concorrente porque outro cliente reservou a data e a capacidade do horário se esgotou. Ninguém recusou o cliente: a agenda é que acabou.
_Evitar_: recusa, data indisponível (é só o texto do aviso)

**Conclusão**:
O fim natural de uma reserva quando a data do evento passa. Não atesta que o serviço foi entregue: um não comparecimento é reclamado pela avaliação, que só fica disponível depois da conclusão.
_Evitar_: finalização, entrega

**Cancelamento de reserva**:
O desfazimento de uma reserva pelo cliente ou pelo prestador, com motivo obrigatório e só dentro do prazo de cancelamento. É o que aciona as arras.
_Evitar_: cancelamento de evento, desistência, estorno (estorno é o movimento de dinheiro, não a decisão)

**Cancelamento de evento**:
O encerramento de um evento inteiro pelo cliente. Encerra os pedidos que ainda não foram reservados e não é permitido enquanto houver reserva.
_Evitar_: cancelar a festa, cancelamento de reserva

## Agenda

**Agenda**:
Os intervalos em que um prestador está ocupado, seja por reservas, seja por bloqueios.
_Evitar_: calendário, disponibilidade

**Capacidade**:
Quantos eventos um prestador atende ao mesmo tempo. Um horário só fica ocupado quando as reservas nele atingem a capacidade.
_Evitar_: vagas, eventos simultâneos (fora do código)

**Folga**:
O tempo livre que o prestador exige antes e depois de cada evento, somado ao intervalo na hora de ver se há conflito.
_Evitar_: intervalo, margem, buffer

**Bloqueio**:
Um intervalo que o próprio prestador marca como indisponível na agenda, sem pedido envolvido.
_Evitar_: folga, indisponibilidade

## Reputação

**Avaliação**:
A nota e o comentário que o cliente dá a um pedido concluído. Há uma por pedido.
_Evitar_: review, feedback

**Favorito**:
Um prestador que o cliente marcou para encontrar depois.
_Evitar_: salvo, curtida
