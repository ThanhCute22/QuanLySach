package HoangThiMyThanh.QuanLySach.controller;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

/**
 * Tắt mapping trực tiếp để tránh trùng với BasicErrorController.
 * Lưu lại helper method nếu cần dùng lại về sau.
 */
@ConditionalOnMissingBean(name = "basicErrorController")
@Component
public class AppErrorController {

    // helper không phải handler - không có @RequestMapping
    public String renderAppError(HttpServletRequest request, Model model) {
        Object message = request.getAttribute("javax.servlet.error.message");
        Object status = request.getAttribute("javax.servlet.error.status_code");
        String detail = (message != null) ? message.toString() : "Không có chi tiết lỗi";
        if (status != null) {
            detail = String.format("HTTP %s - %s", status.toString(), detail);
        }
        model.addAttribute("message", detail);
        return "error/error";
    }
}
