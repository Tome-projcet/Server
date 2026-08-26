package com.Tome.tome.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {
    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }

    @GetMapping("/main")
    public String main(){
        return "main";
    }


    @GetMapping("articles")
    public String articles(){
        return "articles";
    }
}
