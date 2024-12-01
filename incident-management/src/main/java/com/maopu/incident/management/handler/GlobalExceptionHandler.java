package com.maopu.incident.management.handler;

import com.maopu.incident.management.exception.ServiceException;
import com.maopu.incident.management.response.Response;
import com.maopu.incident.management.response.ResponseEnum;
import com.maopu.incident.management.response.ResponseFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
@ConditionalOnClass(ResponseFactory.class)
public class GlobalExceptionHandler {

    /**
     * 自定义异常
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseEntity<Response> handleRrException(ServiceException e) {
        log.error("Handling ServiceException Error Message: {}", e.getMessage(), e);
        return new ResponseEntity(ResponseFactory.getError(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * validate方法级参数校验异常处理
     *
     * @param e
     * @param httpServletResponse
     * @return
     */
    @ExceptionHandler(value = {BindException.class, ValidationException.class, MethodArgumentNotValidException.class})
    @ResponseBody
    public ResponseEntity<Response> validExceptionHandle(Exception e, HttpServletResponse httpServletResponse) {
        try {
            String message = "";
            if (e instanceof MethodArgumentNotValidException) {
                BindingResult bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
                // getFieldError获取的是第一个不合法的参数(P.S.如果有多个参数不合法的话)
                FieldError fieldError = bindingResult.getFieldError();
                if (fieldError != null) {
                    log.error("Input data check failure object: {} ,field: {},errorMessage: {}", fieldError.getObjectName(), fieldError.getField(), fieldError.getDefaultMessage());
                    message = fieldError.getDefaultMessage();
                }
            } else if (e instanceof BindException) {
                // getFieldError获取的是第一个不合法的参数(P.S.如果有多个参数不合法的话)
                FieldError fieldError = ((BindException) e).getFieldError();
                if (fieldError != null) {
                    log.error("Input data check failure object: {} ,field: {},errorMessage: {}", fieldError.getObjectName(), fieldError.getField(), fieldError.getDefaultMessage());
                    message = fieldError.getDefaultMessage();
                }
            } else if (e instanceof ConstraintViolationException) {
                /*
                 * ConstraintViolationException的e.getMessage()形如
                 *     {方法名}.{参数名}: {message}
                 *  这里只需要取后面的message即可
                 */
                String msg = e.getMessage();
                log.error("Input data check failure message: {}", msg);
                if (StringUtils.isNotBlank(msg)) {
                    int lastIndex = msg.lastIndexOf(':');
                    if (lastIndex >= 0) {
                        message = msg.substring(lastIndex + 1).trim();
                    }
                }
            }
            if (StringUtils.isNotBlank(message)) {
                return new ResponseEntity(ResponseFactory.getError(message), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            log.error("Handling Exception Error Message: {}", ex.getMessage(), ex);
        }
        return new ResponseEntity(ResponseFactory.getResponse(ResponseEnum.error), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleAll(Exception ex, WebRequest request) {
        log.error("Handling Exception Error Message: {}", ex.getMessage(), ex);
        if (StringUtils.isNotEmpty(ex.getMessage())) {
            return new ResponseEntity<>(ResponseFactory.getError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(ResponseFactory.getError(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}