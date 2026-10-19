# Documentação de domínio

Como as skills de engenharia devem usar a documentação de domínio deste repo ao explorar o código. O layout é **single-context**: um backend só.

## Antes de explorar, leia

- **`GLOSSARY.md`** na raiz do repo.
- **`docs/adr/`**: as ADRs que tocam a área em que você vai mexer.

Se algum desses arquivos não existir, **siga em silêncio**. Não aponte a ausência nem sugira criá-los de antemão. A skill `/domain-modeling` (acessada via `/grill-with-docs` e `/improve-codebase-architecture`) cria esses arquivos sob demanda, quando termos ou decisões forem de fato resolvidos.

## Estrutura

```
/
├── GLOSSARY.md
├── docs/adr/
│   ├── 0001-reserva-so-com-sinal-pago.md
│   └── ...
└── src/
```

## Use o vocabulário do glossário

Quando sua saída nomear um conceito do domínio (título de issue, proposta de refatoração, hipótese, nome de teste), use o termo como está definido no `GLOSSARY.md`. Não troque por sinônimos que o glossário manda evitar.

Se o conceito de que você precisa ainda não está no glossário, isso é um sinal: ou você está inventando linguagem que o projeto não usa (reconsidere), ou há uma lacuna real (anote para o `/domain-modeling`).

## Sinalize conflitos com ADRs

Se a sua saída contradiz uma ADR existente, deixe isso explícito em vez de sobrescrever em silêncio:

> _Contradiz a ADR-0002 (arras derivadas de quem cancelou), mas vale reabrir porque…_
