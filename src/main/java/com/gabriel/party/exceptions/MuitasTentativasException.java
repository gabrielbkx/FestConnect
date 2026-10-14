package com.gabriel.party.exceptions;

import com.gabriel.party.exceptions.enums.ErrorCode;
import lombok.Getter;

import java.time.Duration;

/** 429: além da mensagem, leva quanto falta para liberar (vira o cabeçalho Retry-After). */
@Getter
public class MuitasTentativasException extends AppException {

    private final Duration tempoRestante;

    public MuitasTentativasException(Duration tempoRestante) {
        super(ErrorCode.MUITAS_TENTATIVAS, String.valueOf(minutosParaLiberar(tempoRestante)));
        this.tempoRestante = tempoRestante;
    }

    /** Segundos até liberar, arredondados para cima e nunca zero: é o valor do Retry-After. */
    public long segundosParaLiberar() {
        return segundosArredondadosParaCima(tempoRestante);
    }

    private static long segundosArredondadosParaCima(Duration tempo) {
        return Math.max(1, (tempo.toMillis() + 999) / 1000);
    }

    /** Os minutos da mensagem saem dos mesmos segundos do Retry-After, para os dois nunca discordarem. */
    private static long minutosParaLiberar(Duration tempo) {
        return (segundosArredondadosParaCima(tempo) + 59) / 60;
    }
}
