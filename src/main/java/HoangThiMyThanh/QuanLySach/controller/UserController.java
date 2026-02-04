package HoangThiMyThanh.QuanLySach.controller;

import HoangThiMyThanh.QuanLySach.entities.User;
import HoangThiMyThanh.QuanLySach.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           @NotNull BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "user/register";
        }

        // Kiểm tra username đã tồn tại
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("errors", new String[]{"Username đã tồn tại"});
            return "user/register";
        }

        // Kiểm tra email đã tồn tại
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("errors", new String[]{"Email đã tồn tại"});
            return "user/register";
        }

        try {
            userService.save(user);
            userService.setDefaultRole(user.getUsername());
        } catch (org.springframework.dao.DataAccessException ex) {
            String msg = "Lỗi khi lưu tài khoản: " + (ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage());
            model.addAttribute("errors", new String[]{msg});
            return "user/register";
        } catch (Exception ex) {
            model.addAttribute("errors", new String[]{"Có lỗi xảy ra: " + ex.getMessage()});
            return "user/register";
        }

        return "redirect:/login";
    }
}
