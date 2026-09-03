package com.Tome.tome.controller;


import com.Tome.tome.domain.User;
import com.Tome.tome.repository.UserRepository;
import com.Tome.tome.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class MypageApiController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/api/mypage")
    public ResponseEntity<User> viewmypage(@AuthenticationPrincipal User user){
        return ResponseEntity.status(200).body(user);
    }


    @PostMapping("/api/mypage/profile")
    public ResponseEntity<Void> settingProfile(@AuthenticationPrincipal User user, String url){
        user.setProfileUrl(url);

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/api/mypage/profile/view")
    public ResponseEntity<String> viewProfile(@AuthenticationPrincipal User user){
        String url  = user.getProfileUrl();

        if(url.isEmpty()){
            return ResponseEntity.ok().body(null); //나중에 기본 프로필이 보이도록 설정할꺼임
        }
        else {
            return ResponseEntity.ok().body(url);
        }
    }
}
