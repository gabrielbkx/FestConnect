package com.gabriel.party.dtos.prestador;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import jakarta.validation.constraints.*;

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

        EnderecoDTO endereco,

        @NotNull UUID categoriaId


        ) {}

