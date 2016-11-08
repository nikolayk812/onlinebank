package com.onlinebank.web.rest;

import com.onlinebank.service.exceptions.NotFoundException;
import com.onlinebank.service.exceptions.OperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.io.IOException;

@ControllerAdvice(annotations = RestController.class)
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private final static Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorInfo notFound(HttpServletRequest req, NotFoundException e) {
        log.error("Exception at request " + req.getRequestURL(), e);
        return new ErrorInfo(req.getRequestURL(), e);
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public ErrorInfo conflict(HttpServletRequest req, DataIntegrityViolationException e) throws IOException {
        log.error("Exception at request " + req.getRequestURL(), e);
        return new ErrorInfo(req.getRequestURL(), e);
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)  // 422
    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    ErrorInfo validationError(HttpServletRequest req, ValidationException e) {
        log.error("Exception at request " + req.getRequestURL());
        return new ErrorInfo(req.getRequestURL(), e);
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)      //403
    @ExceptionHandler(OperationException.class)
    @ResponseBody
    @Order(Ordered.HIGHEST_PRECEDENCE + 3)
    ErrorInfo operationError(HttpServletRequest req, OperationException e) {
        log.error("Exception at request " + req.getRequestURL());
        return new ErrorInfo(req.getRequestURL(), e);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)      //400
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    ErrorInfo badRequest(HttpServletRequest req, RuntimeException e) {
        log.error("Exception at request {}" + req.getRequestURL(), e);
        return new ErrorInfo(req.getRequestURL(), e);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ErrorInfo handleError(HttpServletRequest req, Exception e) {
        log.error("Exception at request " + req.getRequestURL(), e);
        return new ErrorInfo(req.getRequestURL(), e);
    }

}