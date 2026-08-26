package com.Tome.tome.controller;


import com.Tome.tome.dto.AddUserRequest;
import com.Tome.tome.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final UserService userService;

    @PostMapping("/user")
    public String signup(AddUserRequest request, RedirectAttributes redirectAttributes){
        try {
            userService.save(request);
        } catch (IllegalArgumentException e) {
            if(e.getMessage().equals("조건이 틀렸습니다")) {
                return "redirect:/signup?error";
            }
            else{
                redirectAttributes.addFlashAttribute("error1", e.getMessage());
                return "redirect:/signup?error1";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }
}
