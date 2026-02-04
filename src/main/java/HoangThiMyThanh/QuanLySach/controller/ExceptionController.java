package HoangThiMyThanh.QuanLySach.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import java.util.Optional;

@ControllerAdvice
public class ExceptionController {
    private static final Logger log = LoggerFactory.getLogger(ExceptionController.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public org.springframework.web.servlet.ModelAndView handleException(@NotNull HttpServletRequest request, Exception ex) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = ex.getMessage();
        log.error("Unhandled exception - status={}, message={}, exception={}", status, message, ex);
        var mav = new org.springframework.web.servlet.ModelAndView("error/error");
        mav.addObject("errorMessage", message);
        return mav;
    }
}
