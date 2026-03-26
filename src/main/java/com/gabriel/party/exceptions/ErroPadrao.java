package com.gabriel.party.exceptions;

import java.time.LocalDateTime;

public record ErroPadrao(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
}

