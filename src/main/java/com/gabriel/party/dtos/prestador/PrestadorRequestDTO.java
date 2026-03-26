package com.gabriel.party.dtos.prestador;

import com.gabriel.party.model.prestador.endereco.Endereco;
import com.gabriel.party.model.prestador.endereco.dto.EnderecoRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record PrestadorRequestDTO(
        @NotBlank String nome,
        @NotBlank @Email(message = " " )
        String email,
        String descricao,
        @NotNull(message = "O número de WhatsApp é obrigatório")

        @Pattern(regexp = "\\d{10,11}", message = "O número de WhatsApp deve " +
                "conter apenas dígitos e ter entre 10 e 11 caracteres")
        String whatsapp,

        EnderecoRequest endereco,
        Double raioAtendimentoKm,
        @NotNull UUID categoriaId
        ) {}

