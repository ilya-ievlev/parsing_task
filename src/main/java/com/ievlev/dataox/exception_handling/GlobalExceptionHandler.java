package com.ievlev.dataox.exception_handling;

import com.ievlev.dataox.dto.AppErrorStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
//@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IOException.class})
    public ResponseEntity<?> handleIOException(IOException ioException){
        return new ResponseEntity<>(new AppErrorStatusDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ioException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({RuntimeException.class})
    // TODO: 23-Jan-24 возможно удалить если не будет нужно и я придумаю что делать с ioException
    public ResponseEntity<?> handleRuntimeException(RuntimeException runtimeException){
        return new ResponseEntity<>(new AppErrorStatusDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), runtimeException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
