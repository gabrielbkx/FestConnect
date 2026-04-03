package com.gabriel.party.dtos.cliente;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ClienteRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotNull(message = "O número de WhatsApp é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "O número de WhatsApp deve conter apenas dígitos e ter entre 10 e 11 caracteres")
        String whatsapp,

        EnderecoDTO endereco
) {}

