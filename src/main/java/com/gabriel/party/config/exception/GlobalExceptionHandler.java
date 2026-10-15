package com.gabriel.party.config.exception;

import com.gabriel.party.exceptions.*;
import com.gabriel.party.exceptions.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<AppErroResponse> handlerAppException(AppException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        AppErroResponse err = new AppErroResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI(),
                errorCode.getCode()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(err);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppErroResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String mensagensErro = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        AppErroResponse err = new AppErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagensErro,
                request.getRequestURI(),
                "erro_de_validacao"
        );
        return ResponseEntity.status(status).body(err);
    }

    // Negação do @PreAuthorize (AuthorizationDeniedException). Sem este handler,
    // ela caía no handler genérico abaixo e virava 500 em vez de 403.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AppErroResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        AppErroResponse err = new AppErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Você não tem permissão para acessar este recurso.",
                request.getRequestURI(),
                "acesso_negado"
        );
        return ResponseEntity.status(status).body(err);
    }

    // Restrição de exclusão do banco (dois aceites simultâneos para o mesmo horário): SQLState 23P01.
    // Só esse caso vira 409; qualquer outra violação de integridade segue como erro inesperado (500).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AppErroResponse> handleViolacaoDeIntegridade(DataIntegrityViolationException e, HttpServletRequest request) {
        if (ehConflitoDeAgenda(e)) {
            ErrorCode errorCode = ErrorCode.PEDIDO_CONFLITO_AGENDA;
            AppErroResponse err = new AppErroResponse(
                    LocalDateTime.now(),
                    errorCode.getStatus().value(),
                    errorCode.getStatus().getReasonPhrase(),
                    errorCode.getMessage(),
                    request.getRequestURI(),
                    errorCode.getCode()
            );
            return ResponseEntity.status(errorCode.getStatus()).body(err);
        }
        return erroInesperado(e, request);
    }

    /** Percorre a cadeia de causas atrás do SQLState 23P01 (exclusion_violation do Postgres). */
    private static boolean ehConflitoDeAgenda(Throwable e) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "23P01".equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppErroResponse> handleException(Exception e, HttpServletRequest request) {
        return erroInesperado(e, request);
    }

    private ResponseEntity<AppErroResponse> erroInesperado(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        AppErroResponse err = new AppErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI(),
                "erro_inesperado"
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<AppErroResponse> handleMuitasTentativas(MuitasTentativasException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        AppErroResponse err = new AppErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI(),
                e.getErrorCode().getCode()
        );
        return ResponseEntity.status(status)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.segundosParaLiberar()))
                .body(err);
    }
}
