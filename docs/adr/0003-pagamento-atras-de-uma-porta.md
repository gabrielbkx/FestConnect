# Pagamento atrás da porta `PagamentoGateway`, rumo a um microserviço

O monólito só conversa com pagamento pela porta `PagamentoGateway`. Hoje a implementação é um `FakeGateway`, que só existe em dev e aprova na hora. Trocar de provedor (Pagar.me, Mercado Pago) é escrever uma nova implementação da porta. Pagamento é o primeiro serviço a ser extraído (#102), com banco próprio, saga cobrar → reservar → compensar e eventos Kafka (`cobranca.aprovada`, `sinal.estornado`), porque o limite dele é o mais limpo e a porta já é a costura.

## Consequências

Hoje a cobrança roda dentro da transação do pedido: se algo falhar depois de `cobrar`, o registro é desfeito e o dinheiro continua cobrado. Com o fake isso é aceitável, mas não com um provedor real. Fica para o desenho da #102 (pagamento pendente gravado antes, idempotência, webhook).
