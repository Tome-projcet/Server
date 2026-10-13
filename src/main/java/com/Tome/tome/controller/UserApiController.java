package com.Tome.tome.controller;


import com.Tome.tome.domain.User;
import com.Tome.tome.dto.AddUserRequest;
import com.Tome.tome.repository.UserRepository;
import com.Tome.tome.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final UserService userService;
    private final UserRepository userRepository;

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

    @GetMapping("/api/main")
    public String main(@AuthenticationPrincipal User user){
        Boolean first = user.getfirst();
        if(first){
            return "redirect:/onbording/nickname";
        }
        else{
            return "redirect:/main";
        }
    }

    @PutMapping("/api/onbording/nickname")
    @Transactional
    public ResponseEntity<User> setting(@AuthenticationPrincipal User user, String nickname){
        User updateuser = user.update(nickname);

        return ResponseEntity.ok().body(updateuser);
    }


    @PutMapping("/api/onbording/writer")
    @Transactional
    public ResponseEntity<User> settingwriter(@AuthenticationPrincipal User user, String writer){
        User updateuser = user.updateWriter(writer);

        return ResponseEntity.ok().body(updateuser);
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }
}
