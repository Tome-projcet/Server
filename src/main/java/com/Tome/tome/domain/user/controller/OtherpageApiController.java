package com.Tome.tome.domain.user.controller;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import com.Tome.tome.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
@RequiredArgsConstructor
public class OtherpageApiController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/api/otherpage/{id}")
    public ResponseEntity<User> findAllOtherpage(@PathVariable Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));

        return ResponseEntity.status(200).body(user);
    }


    @Transactional
    @PutMapping("/api/otherpage/{id}/follow")
    public ResponseEntity<Void> upfollow(@PathVariable Long id, @AuthenticationPrincipal User user){
        userService.upfollwing(user, id);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/api/otherpage/{id}/profile")
    public ResponseEntity<String> viewProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));
        String url  = user.getProfileUrl();

        if(url == null || url.isEmpty()){
            return ResponseEntity.ok().body(null); //나중에 기본 프로필이 보이도록 설정할꺼임
        }
        else {
            return ResponseEntity.ok().body(url);
        }
    }
}