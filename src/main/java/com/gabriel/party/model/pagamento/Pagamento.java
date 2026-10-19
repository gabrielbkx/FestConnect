package com.gabriel.party.model.pagamento;

import com.gabriel.party.model.pagamento.enums.StatusPagamento;
import com.gabriel.party.model.pagamento.enums.TipoPagamento;
import com.gabriel.party.model.pedido.Pedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Um pagamento de um pedido feito através de um gateway (#99). A plataforma só orquestra: registra a
 * cobrança e o seu desfecho, sem tocar no dinheiro. Hoje só existe o SINAL, que é o que reserva a data.
 * O campo {@code gateway} guarda qual provedor processou ("FAKE" no ambiente local), para que trocar
 * de intermediadora não exija mudar o domínio.
 */
@Entity
@Table(name = "tb_pagamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPagamento tipo;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPagamento status;

    // Qual gateway processou a cobrança (ex.: "FAKE"). Guardado para auditoria e para suportar troca de provedor.
    @Column(name = "gateway", nullable = false)
    private String gateway;

    // Identificador da cobrança no gateway (id da transação/charge). Nulo até a cobrança ser criada.
    @Column(name = "id_externo")
    private String idExterno;

    // Link do comprovante/checkout devolvido pelo gateway, quando houver.
    @Column(name = "url_comprovante")
    private String urlComprovante;

    @CreationTimestamp
    @Column(name = "data_hora_criacao", nullable = false, updatable = false)
    private LocalDateTime dataHoraCriacao;

    @UpdateTimestamp
    @Column(name = "data_hora_atualizacao")
    private LocalDateTime dataHoraAtualizacao;
}
