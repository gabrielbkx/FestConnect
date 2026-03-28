package com.gabriel.party.exceptions;

import java.time.LocalDateTime;

public record AppErroResponse(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        String code
) {
}

