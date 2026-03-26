package com.gabriel.party.config.exception;

import com.gabriel.party.exceptions.ErroPadrao;
import com.gabriel.party.exceptions.RecursoDuplicadoException;
import com.gabriel.party.exceptions.RecursoNaoEncontradoException;
import com.gabriel.party.exceptions.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroPadrao> handleRecursoNaoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErroPadrao err = new ErroPadrao(
                LocalDateTime.now(),
                status.value(),
                "Recurso não encontrado",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErroPadrao> handleRecursoDuplicado(RecursoDuplicadoException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErroPadrao err = new ErroPadrao(
                LocalDateTime.now(),
                status.value(),
                "Conflito de dados",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroPadrao> handleRegraNegocio(RegraNegocioException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ErroPadrao err = new ErroPadrao(
                LocalDateTime.now(),
                status.value(),
                "Erro de regra de negócio",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadrao> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String mensagensErro = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErroPadrao err = new ErroPadrao(
                LocalDateTime.now(),
                status.value(),
                "Erro de validação",
                mensagensErro,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadrao> handleException(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErroPadrao err = new ErroPadrao(
                LocalDateTime.now(),
                status.value(),
                "Erro interno do servidor",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }
}
