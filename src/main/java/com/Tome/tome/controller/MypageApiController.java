package com.Tome.tome.controller;


import com.Tome.tome.domain.User;
import com.Tome.tome.repository.UserRepository;
import com.Tome.tome.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class MypageApiController {

    private final UserRepository userRepository;

    @GetMapping("/api/mypage")
    public ResponseEntity<User> viewmypage(@AuthenticationPrincipal User user){
        return ResponseEntity.status(200).body(user);
    }

    @GetMapping("/api/otherpage/{id}")
    public ResponseEntity<User> viewotherpage(@PathVariable Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));

        return ResponseEntity.status(200).body(user);
    }
}
