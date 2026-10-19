package com.gabriel.party.model.pedido;

import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.prestador.Prestador;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_pedidos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_do_evento", nullable = false)
    private LocalDateTime dataEvento;

    // Fim do evento = início + duração do item. Dá o intervalo [início, fim) usado para
    // detectar sobreposição na agenda do prestador (#85). Preenchido no criarPedido/lote.
    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Column(name = "local_do_evento", nullable = false)
    private String localEvento;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "numero_convidados", nullable = false)
    private Integer numeroConvidados;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "valor_do_orcamento")
    private BigDecimal valor;

    // Sinal (arras) em R$ exigido para reservar a data. Calculado no aceite do orçamento a partir de
    // valor × (percentual do prestador). Pagá-lo é o que leva o pedido a RESERVADO (#99).
    @Column(name = "valor_sinal")
    private BigDecimal valorSinal;

    // Prazo para pagar o sinal depois de aceitar o orçamento. Vencido sem pagamento, o pedido expira.
    @Column(name = "prazo_entrada")
    private LocalDateTime prazoEntrada;

    @Column(name = "detalhes_orcamento", columnDefinition = "TEXT")
    private String detalhesOrcamento;

    @Column(name = "validade_orcamento")
    private LocalDateTime validadeOrcamento;

    @Column(name = "prazo_resposta")
    private LocalDateTime prazoResposta;

    @Column(name = "status_pedido", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    @ManyToOne
    private Cliente cliente;
    @ManyToOne
    private Prestador prestador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_catalogo_id", nullable = false)
    private ItemCatalogo itemCatalogo;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @CreationTimestamp
    private LocalDateTime dataHoraCriacao;

    @UpdateTimestamp
    private LocalDateTime dataHoraAtualizacao;
}
