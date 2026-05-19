package com.gabriel.party.model.evento;

import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.evento.enums.StatusEvento;
import com.gabriel.party.model.evento.enums.TipoEvento;
import com.gabriel.party.model.pedido.Pedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_evento")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "tipo_evento", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @Column(name = "cidade", nullable = false)
    private String cidade;

    @Column(name = "bairro", nullable = false)
    private String bairro;

    @Column(name = "endereco", nullable = false)
    private String endereco;

    @Column(name = "numero_convidados", nullable = false)
    private Integer numeroConvidados;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEvento status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "evento")
    private List<Pedido> pedidos;

    @CreationTimestamp
    @Column(name = "data_hora_criacao", updatable = false)
    private LocalDateTime dataHoraCriacao;

    @UpdateTimestamp
    @Column(name = "data_hora_atualizacao")
    private LocalDateTime dataHoraAtualizacao;
}
