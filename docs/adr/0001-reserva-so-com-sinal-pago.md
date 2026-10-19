# A data só é garantida com o sinal pago

Aceitar um orçamento **não** trava a agenda: o pedido vai para `AGUARDANDO_ENTRADA` com 72h para pagar o sinal, e só o pagamento leva a `RESERVADO` e trava o intervalo. Vários clientes podem ter orçamento aceito para o mesmo horário; quem paga primeiro fica com a data e os concorrentes são fechados (o segundo a pagar recebe 409). Travar no aceite deixaria um cliente sem intenção real bloquear a agenda do prestador de graça, e é assim que o setor de eventos já funciona (#99).
