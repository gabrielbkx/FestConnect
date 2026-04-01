package com.gabriel.party.mapper.autenticacao;


import com.gabriel.party.dtos.autenticacao.cadastro.CadastroRequestDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AutenticacaoMapper {

    Usuario toEntity(CadastroRequestDTO dto);

    CadastroResponseDTO toDto(Usuario usuario);


}
