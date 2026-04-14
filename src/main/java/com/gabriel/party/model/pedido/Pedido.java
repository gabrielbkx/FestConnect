package com.gabriel.party.model.pedido;

import com.gabriel.party.model.cliente.Cliente;
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

    @Column(name = "detalhes_orcamento", columnDefinition = "TEXT")
    private String detalhesOrcamento;

    @Column(name = "validade_orcamento")
    private LocalDateTime validadeOrcamento;

    @Column(name = "status_pedido", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    @ManyToOne
    private Cliente cliente;
    @ManyToOne
    private Prestador prestador;

    @CreationTimestamp
    private LocalDateTime dataHoraCriacao;

    @UpdateTimestamp
    private LocalDateTime dataHoraAtualizacao;
}
