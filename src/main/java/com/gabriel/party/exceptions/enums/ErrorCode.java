package com.gabriel.party.exceptions.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {




    // Erros de prestador
    PRESTADOR_NAO_ENCONTRADO("prestador_nao_encontrado", HttpStatus.NOT_FOUND, "Prestador '%id%' não encontrado."),
    PRESTADOR_INATIVO("prestador_inativo", HttpStatus.BAD_REQUEST, "O prestador '%nome%' está inativo e não pode receber novas ações."),
    PRESTADOR_EMAIL_DUPLICADO("prestador_email_duplicado", HttpStatus.CONFLICT, "Já existe um prestador cadastrado com o email '%email%'."),
    JA_EXISTE_POR_CNPJ("cnpj_duplicado", HttpStatus.CONFLICT, "Já existe um cadastro com o CNPJ '%cnpj%'."),
    JA_EXISTE_POR_CPF("cpf_duplicado", HttpStatus.CONFLICT, "Já existe um cadastro com o CPF '%cpf%'."),

    // Erros de Categoria
    CATEGORIA_NAO_ENCONTRADA("categoria_nao_encontrada", HttpStatus.NOT_FOUND, "Categoria '%categoria%' não encontrada."),
    CATEGORIA_NOME_DUPLICADO("categoria_nome_duplicado", HttpStatus.CONFLICT, "Já existe uma categoria cadastrada com o nome '%nome%'."),
    CATEGORIA_EM_USO("categoria_em_uso", HttpStatus.CONFLICT, "A categoria '%categoria%' não pode ser excluída pois existem prestadores vinculados a ela."),

    // Erros de Cliente
    CLIENTE_NAO_ENCONTRADO("cliente_nao_encontrado", HttpStatus.NOT_FOUND, "Cliente '%id%' não encontrado."),
    CLIENTE_EMAIL_DUPLICADO("cliente_email_duplicado", HttpStatus.CONFLICT, "Já existe um cliente cadastrado com o email '%email%'."),

    // Erros de ItemCatalogo
    ITEM_CATALOGO_NAO_ENCONTRADO("item_nao_encontrado", HttpStatus.NOT_FOUND, "Item do catálogo '%id%' não encontrado."),
    ITEM_PRECO_INVALIDO("item_preco_invalido", HttpStatus.BAD_REQUEST, "O preço do item '%nome%' não pode ser negativo."),

    // Erros de Mídia
    MIDIA_NAO_ENCONTRADA("midia_nao_encontrada", HttpStatus.NOT_FOUND, "Mídia '%id%' não encontrada."),
    MIDIA_FORMATO_NAO_SUPORTADO("midia_formato_nao_suportado", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato de arquivo '%formato%' não suportado. Envie apenas imagens ou vídeos."),
    MIDIA_TAMANHO_EXCEDIDO("midia_tamanho_excedido", HttpStatus.PAYLOAD_TOO_LARGE, "O tamanho do arquivo '%nomeArquivo%' excede o limite permitido."),
    ERRO_AO_PROCESSAR_VIDEO( "erro_ao_processar_video", HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro ao processar o vídeo: '%detalhes%'"),
    FORMATO_INVALIDO("formato_invalido", HttpStatus.BAD_REQUEST, "Formato de arquivo '%formato%' não suportado. Envie apenas imagens ou vídeos."),
    LIMITE_MIDIAS_PRESTADOR("limite_midias_prestador", HttpStatus.BAD_REQUEST, "O prestador '%prestadorId%' já atingiu o limite máximo de mídias permitidas."),
    ERRO_SALVAR_MIDIA( "erro_salvar_midia", HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro ao salvar a mídia: '%detalhes%'"),

    // Erros de Avaliação
    AVALIACAO_NAO_ENCONTRADA("avaliacao_nao_encontrada", HttpStatus.NOT_FOUND, "Avaliação '%id%' não encontrada."),
    AVALIACAO_NOTA_INVALIDA("avaliacao_nota_invalida", HttpStatus.BAD_REQUEST, "A nota da avaliação deve estar entre 1 e 5. Valor recebido: '%nota%'."),
    AVALIACAO_DUPLICADA("avaliacao_duplicada", HttpStatus.CONFLICT, "O usuário '%usuario%' já avaliou este serviço anteriormente."),

    // Erros de Geocoding
    GEOCODING_FALHA_COMUNICACAO("geocoding_falha_comunicacao", HttpStatus.SERVICE_UNAVAILABLE, "Erro de comunicação com a API de geocoding: '%detalhes%'"),


    // Regras de negócio
    REGRA_NEGOCIO_VIOLADA("regra_negocio_violada", HttpStatus.BAD_REQUEST, "A ação solicitada viola uma regra de negócio: '%detalhes%'"),

    //S3 bucket
    ERRO_AO_PROCESAR_IMAGEM("erro_ao_processar_imagem", HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro ao processar a imagem: '%detalhes%'"),
    URL_INVALIDA("url_invalida", HttpStatus.BAD_REQUEST, "A URL fornecida '%url%' é inválida."),

    //AUTENTICAÇÃO E AUTORIZAÇÃO
    ERRO_AO_GERAR_TOKEN_JWT("erro_ao_gerar_token_jwt", HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar token)"),
    ERRO_AO_VALIDAR_TOKEN_JWT("erro_ao_validar_token_jwt", HttpStatus.UNAUTHORIZED, "Token JWT inválido ou expirado"),
    USUARIO_JA_EXISTE_POR_EMAIL("usuario_ja_existe_por_email", HttpStatus.CONFLICT, "Já existe um usuário cadastrado com esse email '%email%'")
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
