package com.gabriel.party.dtos.autenticacao.cadastro;

import com.gabriel.party.dtos.autenticacao.enums.TipoPerfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CadastroRequestDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        String nomeCompleto,
        @NotBlank(message = "O email é obrigatório")
        @Email
        String email,
        @NotBlank(message = "A senha é obrigatória")
        String senha,
        @NotBlank(message = "O CPF ou CNPJ é obrigatório")
        String cpfOuCnpj,
        TipoPerfil tipoPerfil
        ){
}
