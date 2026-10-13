package com.Tome.tome.controller;


import com.Tome.tome.domain.User;
import com.Tome.tome.dto.GenreRequest;
import com.Tome.tome.repository.UserRepository;
import com.Tome.tome.service.UserMostService;
import com.Tome.tome.service.UserService;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class MypageApiController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMostService userMostService;

    @GetMapping("/api/mypage")
    public ResponseEntity<User> viewmypage(@AuthenticationPrincipal User user){
        return ResponseEntity.status(200).body(user);
    }

    @Transactional
    @PutMapping("/api/mypage/profile")
    public ResponseEntity<User> settingProfile(@AuthenticationPrincipal User user, String url){
        User updateuser = user.UpdateProfileUrl(url);

        return ResponseEntity.status(201).body(updateuser);
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

    @Transactional
    @PutMapping("/api/mypage/update/nickname")
    public ResponseEntity<User> updateNickname(@AuthenticationPrincipal User user, String nickname){
        User updateuser = user.update(nickname);

        return ResponseEntity.ok().body(updateuser);
    }

    @Transactional
    @PutMapping("/api/mypage/update/profile")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal User user, String url){
        User updateuser = user.UpdateProfileUrl(url);

        return ResponseEntity.ok().body(updateuser);
    }

    @Transactional
    @PutMapping("/api/mypage/update/writer")
    public ResponseEntity<User> updateWriter(@AuthenticationPrincipal User user, String writer){
        User updateuser = user.updateWriter(writer);

        return ResponseEntity.ok().body(updateuser);
    }

    @PostMapping("/api/onbording/genre")
    public ResponseEntity<Void> selectGenre(@AuthenticationPrincipal User user, @RequestBody GenreRequest request){
        userService.checkGenre(user, request.getName());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/mypage/mostgenre")
    public ResponseEntity<Void> mostgenre(@AuthenticationPrincipal User user){
        userMostService.mostGenreset(user);

        return ResponseEntity.status(200).build();
    }
}
