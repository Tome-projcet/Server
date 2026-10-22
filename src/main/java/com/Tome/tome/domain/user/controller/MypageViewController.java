package com.Tome.tome.domain.user.controller;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class MypageViewController {
    private final UserRepository userRepository;

    @GetMapping("/mypage")
    public String viewMYPAGE(@AuthenticationPrincipal User user, Model model){
        model.addAttribute("my_page", user);

        return "my_page";
    }

}


