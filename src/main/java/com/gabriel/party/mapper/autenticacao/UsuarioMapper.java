package com.gabriel.party.mapper.autenticacao;

import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {


    CadastroResponseDTO toDto(Usuario usuario);

    Usuario toUsuarioPrestador(CadastroPrestadorDTO dto);

    Prestador toPrestador(CadastroPrestadorDTO dto);

    Cliente toCliente(CadastroClienteDTO dto);

    Usuario toUsuarioCliente(CadastroClienteDTO dto);

}