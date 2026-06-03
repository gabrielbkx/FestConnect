# FestConnect — Design System (Base v1)

> Foco em usabilidade para um **marketplace de prestadores de serviços para festas**.
> Usuário-alvo: cliente buscando contratar (busca, compara, pede orçamento) e prestador divulgando seu trabalho.
> Inspirações visuais analisadas: Airbnb, OLX e marketplaces de imóveis (cards + grade + CTAs em destaque).

---

## 1. Princípios de Design

1. **Confiança primeiro** — fotos grandes, avaliações visíveis, informação transparente.
2. **Hierarquia clara** — o usuário deve saber em 1 segundo o que clicar.
3. **Mobile-first** — maioria do tráfego em celular, layout precisa funcionar em telas pequenas.
4. **Branco respira** — fundo claro, contraste alto, sem poluição visual.
5. **CTA inequívoco** — apenas 1 botão primário por bloco de decisão.

---

## 2. Cores

### Paleta principal

| Token | Hex | Uso |
|---|---|---|
| `--color-primary` | `#E94560` | CTAs, links de ação, badges de destaque, ícones ativos |
| `--color-primary-hover` | `#D63852` | Hover do primário (10% mais escuro) |
| `--color-primary-soft` | `#FDECEF` | Backgrounds suaves, chips selecionados, notificações |
| `--color-dark` | `#1A1A2E` | Títulos, texto de alto contraste, header (opcional) |
| `--color-text` | `#1A1A2E` | Texto principal |
| `--color-text-muted` | `#6B7280` | Texto secundário, labels, placeholders, metadados |
| `--color-white` | `#FFFFFF` | Background principal, superfícies de cards |

### Cores de suporte (derivadas, para feedback do sistema)

| Token | Hex | Uso |
|---|---|---|
| `--color-bg` | `#F9FAFB` | Background secundário (seções alternadas) |
| `--color-border` | `#E5E7EB` | Bordas de cards, inputs, divisórias |
| `--color-border-strong` | `#D1D5DB` | Borda em foco de inputs |
| `--color-success` | `#10B981` | Confirmações, pedidos aceitos |
| `--color-warning` | `#F59E0B` | Pedidos pendentes, avisos |
| `--color-danger` | `#DC2626` | Erros, cancelamentos |
| `--color-rating` | `#F59E0B` | Estrelas de avaliação |

### Regras de uso

- **Primário (#E94560)** nunca em blocos grandes de fundo — usar somente em botões, ícones, badges e links. Aplicar em áreas grandes cansa a vista.
- **Dark (#1A1A2E)** pode ser usado no header (modo escuro) e no footer, mas no corpo manter fundo branco.
- **Contraste mínimo** texto sobre branco: usar `--color-text` ou `--color-text-muted`. Nunca usar `--color-primary` como cor de texto longo.

---

## 3. Tipografia

### Fonte principal

**Inter** (Google Fonts) — geometria limpa, excelente legibilidade em telas, gratuita.

```
font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
```

### Fonte secundária (opcional, para títulos com personalidade)

**Poppins** ou **Plus Jakarta Sans** — para H1/H2 marcantes. Se preferir um único peso visual, manter apenas Inter.

### Escala tipográfica

| Token | Tamanho | Peso | Line-height | Uso |
|---|---|---|---|---|
| `--text-display` | 56px / 3.5rem | 700 | 1.1 | Hero da landing (desktop) |
| `--text-h1` | 40px / 2.5rem | 700 | 1.15 | Títulos de página |
| `--text-h2` | 32px / 2rem | 700 | 1.2 | Seções da home, headlines |
| `--text-h3` | 24px / 1.5rem | 600 | 1.3 | Subtítulos, cards grandes |
| `--text-h4` | 20px / 1.25rem | 600 | 1.35 | Título de card, modal |
| `--text-lg` | 18px / 1.125rem | 500 | 1.5 | Texto destacado |
| `--text-base` | 16px / 1rem | 400 | 1.6 | Corpo de texto padrão |
| `--text-sm` | 14px / 0.875rem | 400 | 1.5 | Texto auxiliar, metadados |
| `--text-xs` | 12px / 0.75rem | 500 | 1.4 | Labels, badges, captions |

### Mobile (≤ 768px)

Reduzir display/H1/H2 em ~25%:
- `--text-display` → 36px
- `--text-h1` → 28px
- `--text-h2` → 24px

### Regras

- **H1 único por página** — sempre o título principal.
- **Pesos**: usar 400 (regular), 500 (medium), 600 (semibold) e 700 (bold). Evitar Light e ExtraBold.
- **Não usar maiúsculas** em textos longos (apenas em micro-labels e badges).
- **Letter-spacing**: -0.02em em títulos grandes (display/H1) para parecerem mais firmes.

---

## 4. Espaçamento

Sistema base de **4px** (múltiplos de 4).

| Token | Valor | Uso |
|---|---|---|
| `--space-1` | 4px | Gap entre ícone e label |
| `--space-2` | 8px | Padding interno pequeno |
| `--space-3` | 12px | Padding em chips, badges |
| `--space-4` | 16px | Padding em cards, botões |
| `--space-5` | 24px | Espaçamento entre elementos relacionados |
| `--space-6` | 32px | Separação de blocos dentro de uma seção |
| `--space-8` | 48px | Padding vertical de seções (mobile) |
| `--space-10` | 64px | Padding vertical de seções (desktop) |
| `--space-12` | 96px | Separação entre grandes blocos |

### Container

- **Max-width**: 1280px, centralizado
- **Padding lateral**: 16px (mobile) / 24px (tablet) / 32px (desktop)

### Grid

- Listagens de prestadores: **2 colunas mobile**, **3 colunas tablet**, **4 colunas desktop**
- Gap entre cards: 16px (mobile), 24px (desktop)

---

## 5. Bordas e Arredondamento

| Token | Valor | Uso |
|---|---|---|
| `--radius-sm` | 6px | Inputs, badges pequenas |
| `--radius-md` | 10px | Botões |
| `--radius-lg` | 16px | Cards, imagens dentro de cards |
| `--radius-xl` | 24px | Containers grandes, modais |
| `--radius-full` | 9999px | Avatares, chips de categoria, pílulas |

**Decisão**: arredondamento médio-alto, seguindo o padrão Airbnb/OLX — passa sensação de aconchego e modernidade, sem ser infantil.

---

## 6. Sombras

| Token | Valor | Uso |
|---|---|---|
| `--shadow-none` | none | Cards sem elevação (mais clean) |
| `--shadow-sm` | `0 1px 2px rgba(17, 24, 39, 0.05)` | Inputs, divisórias sutis |
| `--shadow-md` | `0 4px 12px rgba(17, 24, 39, 0.08)` | Cards padrão, dropdowns |
| `--shadow-lg` | `0 12px 32px rgba(17, 24, 39, 0.12)` | Modais, popovers, hover de cards |
| `--shadow-focus` | `0 0 0 3px rgba(233, 69, 96, 0.25)` | Foco de inputs e botões (acessibilidade) |

**Regra**: cards em listagem podem **não ter sombra** (só borda) — quando hover, ganham `--shadow-md` + leve translateY(-2px).

---

## 7. Componentes

### 7.1 Botões

#### Variantes

**Primário (`btn-primary`)**
- Background: `#E94560`
- Cor texto: `#FFFFFF`
- Border-radius: 10px
- Padding: 12px 24px (md) / 14px 32px (lg)
- Font-weight: 600
- Hover: background `#D63852` + `--shadow-md`
- Focus: `--shadow-focus`
- Disabled: opacity 0.5, cursor not-allowed

**Secundário (`btn-secondary`)**
- Background: `#FFFFFF`
- Cor texto: `#1A1A2E`
- Border: 1px solid `#D1D5DB`
- Hover: border `#1A1A2E` + background `#F9FAFB`

**Ghost / Tertiary (`btn-ghost`)**
- Background: transparente
- Cor texto: `#1A1A2E`
- Hover: background `#F9FAFB`

**Destrutivo (`btn-danger`)**
- Background: `#DC2626`
- Cor texto: `#FFFFFF`

#### Tamanhos

| Tamanho | Padding | Font-size | Altura |
|---|---|---|---|
| sm | 8px 16px | 14px | 36px |
| md | 12px 24px | 16px | 44px |
| lg | 14px 32px | 16px | 52px |

**Acessibilidade**: altura mínima 44px no mobile (tap target).

---

### 7.2 Inputs de Formulário

#### Estilo padrão

- Background: `#FFFFFF`
- Border: 1px solid `#E5E7EB`
- Border-radius: 10px
- Padding: 12px 16px
- Font-size: 16px (evita zoom no iOS)
- Cor texto: `#1A1A2E`
- Placeholder: `#6B7280`
- Altura: 48px (md)

#### Estados

- **Hover**: border `#D1D5DB`
- **Focus**: border `#E94560` + `--shadow-focus`
- **Erro**: border `#DC2626` + mensagem abaixo em vermelho 14px
- **Disabled**: background `#F9FAFB`, texto `#6B7280`

#### Label

- Acima do input, 14px, weight 500, cor `#1A1A2E`
- Margin-bottom: 6px
- Asterisco vermelho `#E94560` para campos obrigatórios

#### Helper text

- Abaixo do input, 12px, cor `#6B7280`

#### Barra de busca da home (caso especial)

- Container branco com sombra `--shadow-md`
- Border-radius: `--radius-full` (pílula grande, estilo Airbnb)
- Campos divididos por divisórias verticais sutis (`#E5E7EB`)
- Botão de busca circular `--color-primary` no canto direito (ícone lupa)

---

### 7.3 Cards (prestador / serviço)

**Estrutura padrão:**

```
┌─────────────────────────┐
│                         │
│      [imagem 4:3]    ♡  │  ← imagem cobre topo, heart no canto sup. dir.
│                         │
│  ─────────────────────  │
│  Nome do Prestador   ★4.9 (124) │  ← título + rating na mesma linha
│  Categoria · Cidade            │  ← metadado em cinza
│                                │
│  A partir de R$ 800           │  ← preço destacado
└─────────────────────────┘
```

**Especificações:**
- Background: `#FFFFFF`
- Border: 1px solid `#E5E7EB` (ou nenhuma + `--shadow-sm`)
- Border-radius: `--radius-lg` (16px)
- Padding interno (área de texto): 16px
- Imagem: aspect-ratio 4:3, `object-fit: cover`, border-radius 16px no topo
- Hover: `translateY(-2px)` + `--shadow-md`, transition 200ms
- Título: 16px, weight 600, cor `#1A1A2E`, max 1 linha + ellipsis
- Metadados: 14px, cor `#6B7280`
- Preço: 16px, weight 700, cor `#1A1A2E`
- Rating: estrela `#F59E0B` + número 14px weight 500 + total entre parênteses em `#6B7280`

**Badges sobre a imagem** (canto sup. esq.):
- "Destaque" — fundo `#1A1A2E`, texto branco
- "Novo" — fundo `#10B981`, texto branco
- "Mais contratado" — fundo `#E94560`, texto branco
- Padding 4px 10px, radius 999px, font 12px weight 600

**Botão favoritar (heart):**
- Canto sup. dir. da imagem
- Círculo branco 32x32, sombra sutil
- Ícone cinza quando vazio, `#E94560` quando favoritado

---

### 7.4 Chips / Tags / Pílulas

Usados para categorias, filtros e tipos de evento.

- Padding: 8px 16px
- Border-radius: `--radius-full`
- Background padrão: `#FFFFFF`, border `#E5E7EB`
- Texto: 14px, weight 500
- **Selecionado**: background `--color-primary-soft` (#FDECEF), texto `#E94560`, border `#E94560`
- **Hover**: background `#F9FAFB`

---

### 7.5 Avatares

- Pequeno: 32x32
- Médio: 48x48
- Grande: 80x80 (perfil)
- Sempre `border-radius: 50%`
- Fallback: iniciais em fundo `#1A1A2E` com texto branco

---

### 7.6 Header (topo)

- Altura: 72px
- Background: `#FFFFFF`
- Border-bottom: 1px solid `#E5E7EB`
- Sticky no topo
- Logo à esquerda, navegação central, ações à direita
- Sombra ao rolar: `--shadow-sm`

---

### 7.7 Modais

- Background: `#FFFFFF`
- Border-radius: `--radius-xl` (24px)
- Padding: 32px
- Overlay: `rgba(26, 26, 46, 0.5)`
- Sombra: `--shadow-lg`
- Max-width: 560px (forms) / 800px (conteúdo)

---

## 8. Iconografia

**Biblioteca recomendada**: [Lucide Icons](https://lucide.dev) (gratuita, leve, mesma família visual do Inter).

- Tamanho padrão: 20px (em botões/inputs), 24px (em headers), 16px (inline com texto)
- Stroke-width: 2
- Cor: herda do contexto (`currentColor`)

---

## 9. Acessibilidade

- Contraste mínimo **4.5:1** para texto normal, **3:1** para texto grande
- `#E94560` sobre branco: contraste 4.6 ✅
- `#6B7280` sobre branco: contraste 4.7 ✅ (limite — não usar para texto pequeno crítico)
- Foco visível em todos os elementos interativos (`--shadow-focus`)
- Tap targets mínimos: 44x44px no mobile
- Labels sempre associadas aos inputs
- Estados `aria-*` para botões de favoritar, dropdowns, modais

---

## 10. Tokens CSS (snippet pronto)

```css
:root {
  /* Cores */
  --color-primary: #E94560;
  --color-primary-hover: #D63852;
  --color-primary-soft: #FDECEF;
  --color-dark: #1A1A2E;
  --color-text: #1A1A2E;
  --color-text-muted: #6B7280;
  --color-white: #FFFFFF;
  --color-bg: #F9FAFB;
  --color-border: #E5E7EB;
  --color-border-strong: #D1D5DB;
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-danger: #DC2626;
  --color-rating: #F59E0B;

  /* Tipografia */
  --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;

  /* Espaçamento */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 24px;
  --space-6: 32px;
  --space-8: 48px;
  --space-10: 64px;

  /* Raios */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 9999px;

  /* Sombras */
  --shadow-sm: 0 1px 2px rgba(17, 24, 39, 0.05);
  --shadow-md: 0 4px 12px rgba(17, 24, 39, 0.08);
  --shadow-lg: 0 12px 32px rgba(17, 24, 39, 0.12);
  --shadow-focus: 0 0 0 3px rgba(233, 69, 96, 0.25);

  /* Transições */
  --transition-fast: 150ms ease;
  --transition-base: 200ms ease;
}
```

---

## 11. Referências analisadas

- **Airbnb** — barra de busca em pílula, cards com heart e badges "Preferido", layout em carrosséis horizontais
- **OLX** — categorias com ícones em linha horizontal, CTA laranja vibrante, card de produto com preço grande
- **Marketplaces de imóveis** — grade limpa, imagem dominante, info enxuta abaixo

**O que NÃO copiar:**
- Excesso de cores e ruído visual da OLX (banners promocionais coloridos)
- Header escuro pesado de alguns marketplaces — manter branco para parecer mais leve e premium

---

## Próximos passos sugeridos

1. Validar tokens com a equipe (especialmente arredondamento e sombra preferida)
2. Construir componentes-base no React (Button, Input, Card, Chip, Badge)
3. Aplicar tokens via CSS vars no projeto Vite existente
4. Refazer telas começando pela Home com a nova identidade
