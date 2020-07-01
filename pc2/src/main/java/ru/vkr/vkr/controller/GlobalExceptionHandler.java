package ru.vkr.vkr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import ru.vkr.vkr.domain.exception.Pc2Exception;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@ControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Pc2Exception.class)
    public ModelAndView handleBadFileNameException(Exception ex) {
        try {
            request.logout();
            SecurityContextHolder.clearContext();
        } catch (ServletException e) {
            e.printStackTrace();
        } finally {
            ModelAndView modelAndView = new ModelAndView("/error/pc2error");
            return modelAndView;
        }
    }
}