package com.etu.schedule.exception.handler;

import lombok.extern.log4j.Log4j2;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Log4j2
@ControllerAdvice
public class TelegramExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoHttpResponseException.class)
    public void handlerNoHttpResponse(NoHttpResponseException ex){}

    @ExceptionHandler(SocketTimeoutException.class)
    public void handlerSocketTimeout(SocketTimeoutException ex){}

    @ExceptionHandler(ConnectException.class)
    public void handlerConnectException(ConnectException ex){}

    @ExceptionHandler(ConnectTimeoutException.class)
    public void handlerConnectTimeoutException(ConnectTimeoutException ex){}

    @ExceptionHandler(HttpHostConnectException.class)
    public void handlerHttpHostConnectException(HttpHostConnectException ex){}

}
