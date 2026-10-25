package com.Tome.tome.domain.user.controller;

import com.Tome.tome.domain.genre.dto.GenreRequest;
import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.port.in.UserMostUseCase;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import com.Tome.tome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class MypageApiController {

    private final UserUseCase userUseCase;
    private final UserMostUseCase userMostUseCase;
    private final UserRepository userRepository;

    @GetMapping("/api/mypage")
    public ResponseEntity<PageUserResponse> viewmypage(@AuthenticationPrincipal User user){
        User user1 = userRepository.findById(user.getId()).orElseThrow();
        PageUserResponse pageUserResponse = PageUserResponse.from(user1);
        return ResponseEntity.status(200).body(pageUserResponse);
    }

    @PutMapping("/api/mypage/profile")
    public ResponseEntity<PageUserResponse> settingProfile(@AuthenticationPrincipal User user, String url){
        User updateuser = userUseCase.updateProfileUrl(user.getId(), url);

        return ResponseEntity.status(201).body(PageUserResponse.from(updateuser));
    }

    @GetMapping("/api/mypage/profile/view")
    public ResponseEntity<String> viewProfile(@AuthenticationPrincipal User user){
        String url  = userUseCase.findById(user.getId()).getProfileUrl();

        if(url == null || url.isEmpty()){
            return ResponseEntity.ok().body(null); //나중에 기본 프로필이 보이도록 설정할꺼임
        }
        else {
            return ResponseEntity.ok().body(url);
        }
    }

    @PutMapping("/api/mypage/update/nickname")
    public ResponseEntity<PageUserResponse> updateNickname(@AuthenticationPrincipal User user, String nickname){
        try{
            User updateuser = userUseCase.updateNickname(user.getId(), nickname);
            return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
        } catch (IllegalArgumentException i){
            return ResponseEntity.status(404).build();
        }
    }

    @PutMapping("/api/mypage/update/profile")
    public ResponseEntity<PageUserResponse> updateProfile(@AuthenticationPrincipal User user, String url){
        User updateuser = userUseCase.updateProfileUrl(user.getId(), url);

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }

    @PutMapping("/api/mypage/update/writer")
    public ResponseEntity<PageUserResponse> updateWriter(@AuthenticationPrincipal User user, String writer){
        User updateuser = userUseCase.updateWriter(user.getId(), writer);

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }

    @PostMapping("/api/onbording/genre")
    public ResponseEntity<Void> selectGenre(@AuthenticationPrincipal User user, @RequestBody GenreRequest request){
        userUseCase.checkGenre(user, request.getName());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/mypage/mostgenre")
    public ResponseEntity<Void> mostgenre(@AuthenticationPrincipal User user){
        userMostUseCase.mostGenreset(user);

        return ResponseEntity.status(200).build();
    }
}
