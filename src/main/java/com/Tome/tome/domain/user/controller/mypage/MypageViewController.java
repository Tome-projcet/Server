package com.Tome.tome.domain.user.controller.mypage;

import com.Tome.tome.domain.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageViewController {

    @GetMapping("/mypage")
    public String viewMYPAGE(@AuthenticationPrincipal User user, Model model){
        model.addAttribute("my_page", user);

        return "my_page";
    }
}
