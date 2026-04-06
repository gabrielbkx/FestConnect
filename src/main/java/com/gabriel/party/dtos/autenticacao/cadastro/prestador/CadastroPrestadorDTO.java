package com.gabriel.party.dtos.autenticacao.cadastro.prestador;

import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record CadastroPrestadorDTO(
        @NotBlank(message = "O nome da empresa ou profissional é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "O email é obrigatório")
        @Email
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha,

        @NotBlank(message = "O número de WhatsApp é obrigatório")
        String whatsapp,

        @NotBlank(message = "O CNPJ ou CPF é obrigatório")
        @Pattern(regexp = "(^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$)|(^\\d{11}$)|(^\\d{2}\\.\\d{3}\\.\\d{3}/" +
                "\\d{4}-\\d{2}$)|(^\\d{14}$)", message = "Formato de CPF ou CNPJ inválido")
        String cnpjOuCpf,

        @NotNull(message = "A categoria de serviço é obrigatória")
        UUID categoriaId,

        EnderecoDTO endereco
) {}