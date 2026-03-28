package com.gabriel.party.exceptions.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {




    // Erros de prestador
    PRESTADOR_NAO_ENCONTRADO("prestador_nao_encontrado", HttpStatus.NOT_FOUND, "Prestador '%id%' não encontrado."),
    PRESTADOR_INATIVO("prestador_inativo", HttpStatus.BAD_REQUEST, "O prestador '%nome%' está inativo e não pode receber novas ações."),
    PRESTADOR_EMAIL_DUPLICADO("prestador_email_duplicado", HttpStatus.CONFLICT, "Já existe um prestador cadastrado com o email '%email%'."),

    // Erros de Categoria
    CATEGORIA_NAO_ENCONTRADA("categoria_nao_encontrada", HttpStatus.NOT_FOUND, "Categoria '%categoria%' não encontrada."),
    CATEGORIA_NOME_DUPLICADO("categoria_nome_duplicado", HttpStatus.CONFLICT, "Já existe uma categoria cadastrada com o nome '%nome%'."),
    CATEGORIA_EM_USO("categoria_em_uso", HttpStatus.CONFLICT, "A categoria '%categoria%' não pode ser excluída pois existem prestadores vinculados a ela."),

    // Erros de ItemCatalogo
    ITEM_CATALOGO_NAO_ENCONTRADO("item_nao_encontrado", HttpStatus.NOT_FOUND, "Item do catálogo '%id%' não encontrado."),
    ITEM_PRECO_INVALIDO("item_preco_invalido", HttpStatus.BAD_REQUEST, "O preço do item '%nome%' não pode ser negativo."),

    // Erros de Mídia
    MIDIA_NAO_ENCONTRADA("midia_nao_encontrada", HttpStatus.NOT_FOUND, "Mídia '%id%' não encontrada."),
    MIDIA_FORMATO_NAO_SUPORTADO("midia_formato_nao_suportado", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato de arquivo '%formato%' não suportado. Envie apenas imagens ou vídeos."),
    MIDIA_TAMANHO_EXCEDIDO("midia_tamanho_excedido", HttpStatus.PAYLOAD_TOO_LARGE, "O tamanho do arquivo '%nomeArquivo%' excede o limite permitido."),

    // Erros de Avaliação
    AVALIACAO_NAO_ENCONTRADA("avaliacao_nao_encontrada", HttpStatus.NOT_FOUND, "Avaliação '%id%' não encontrada."),
    AVALIACAO_NOTA_INVALIDA("avaliacao_nota_invalida", HttpStatus.BAD_REQUEST, "A nota da avaliação deve estar entre 1 e 5. Valor recebido: '%nota%'."),
    AVALIACAO_DUPLICADA("avaliacao_duplicada", HttpStatus.CONFLICT, "O usuário '%usuario%' já avaliou este serviço anteriormente."),

    // Erros de Geocoding
    GEOCODING_FALHA_COMUNICACAO("geocoding_falha_comunicacao", HttpStatus.SERVICE_UNAVAILABLE, "Erro de comunicação com a API de geocoding: '%detalhes%'"),


    // Regras de negócio
    REGRA_NEGOCIO_VIOLADA("regra_negocio_violada", HttpStatus.BAD_REQUEST, "A ação solicitada viola uma regra de negócio: '%detalhes%'")
    ;


    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

}
