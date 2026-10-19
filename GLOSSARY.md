# Resenha (FestConnect)

Marketplace que conecta quem organiza uma festa a quem presta serviços para ela, do pedido de orçamento até o evento acontecer.

## Pessoas

**Cliente**:
Quem organiza o evento e contrata serviços.
_Evitar_: contratante, comprador, usuário (usuário é só a conta de login)

**Prestador**:
Quem oferece serviços, produtos ou locais para eventos. Na interface aparece como "parceiro".
_Evitar_: fornecedor, vendedor

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
A festa do cliente, que agrupa os pedidos feitos para ela.
_Evitar_: festa (na linguagem do código), ocasião

**Pedido**:
A solicitação de um cliente por um item de catálogo, para uma data e um intervalo de horário. Passa por orçamento, sinal e reserva até ser concluído ou encerrado.
_Evitar_: contrato, solicitação, booking

**Orçamento**:
O valor que o prestador propõe para um pedido. O cliente aceita ou recusa.
_Evitar_: cotação, proposta

**Sinal**:
O percentual do orçamento que o cliente paga para garantir a data. Cada prestador define seu percentual.
_Evitar_: entrada (só no nome do status "aguardando entrada"), adiantamento, depósito

**Reserva**:
O estado do pedido depois que o sinal foi pago: a data passa a ser do cliente e a agenda do prestador fica travada nesse intervalo.
_Evitar_: confirmação, agendamento

**Arras**:
A regra sobre o destino do sinal quando uma reserva é cancelada: se o cliente desiste, perde o sinal; se o prestador desiste, devolve.
_Evitar_: multa, penalidade

**Concorrentes**:
Os outros pedidos que disputam o mesmo intervalo na agenda do mesmo prestador. Quem paga o sinal primeiro fica com a data, e os concorrentes são fechados.
_Evitar_: conflitantes, duplicados

**Conclusão**:
O fim natural de uma reserva: o evento passou. Só depois dela o cliente pode avaliar.
_Evitar_: finalização, entrega

**Cancelamento de evento**:
O encerramento de uma reserva por decisão do cliente ou do prestador, com motivo obrigatório e só dentro do prazo de cancelamento. Não se confunde com cancelar o pedido antes da reserva, nem com cancelar o evento (a festa) inteiro.
_Evitar_: desistência, estorno (estorno é o movimento de dinheiro, não a decisão)

## Agenda

**Agenda**:
Os intervalos em que um prestador está ocupado, seja por reservas, seja por bloqueios.
_Evitar_: calendário, disponibilidade

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
