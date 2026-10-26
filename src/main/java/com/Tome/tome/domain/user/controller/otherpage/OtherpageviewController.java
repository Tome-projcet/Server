package com.Tome.tome.domain.user.controller.otherpage;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class OtherpageviewController {

    private final UserUseCase userUseCase;

    @GetMapping("/otherpage/{id}")
    public String viewOtherPage(@PathVariable Long id, Model model){
        User user = userUseCase.findById(id);

        model.addAttribute("otherpage", user);

        return "otherpage";
    }
}
