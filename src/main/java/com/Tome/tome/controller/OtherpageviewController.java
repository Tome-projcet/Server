package com.Tome.tome.controller;


import com.Tome.tome.domain.User;
import com.Tome.tome.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class OtherpageviewController {

    private final UserRepository userRepository;

    @GetMapping("/otherpage/{id}")
    public String viewOtherPage(@PathVariable Long id, Model model){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));

        model.addAttribute("otherpage", user);

        return "otherpage";
    }
}
