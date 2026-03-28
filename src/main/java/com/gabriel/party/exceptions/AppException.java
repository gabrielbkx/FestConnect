package com.gabriel.party.exceptions;

import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.exceptions.utils.FormatadorDeMensagemDeErro;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode, String... args) {
        super(FormatadorDeMensagemDeErro.formatarMensagem(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }

}