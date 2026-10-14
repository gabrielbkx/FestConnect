package com.gabriel.party.exceptions;

import com.gabriel.party.exceptions.enums.ErrorCode;
import lombok.Getter;

import java.time.Duration;

@Getter
public class MuitasTentativasException extends AppException {

    private final Duration tempoRestante;

    public MuitasTentativasException(Duration tempoRestante) {
        super(ErrorCode.MUITAS_TENTATIVAS, String.valueOf(minutosArredondadosParaCima(tempoRestante)));
        this.tempoRestante = tempoRestante;
    }

    private static long minutosArredondadosParaCima(Duration tempo) {
        return Math.max(1, (tempo.toSeconds() + 59) / 60);
    }
}
