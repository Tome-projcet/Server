package com.Tome.tome.domain.user.controller;

import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
@RequiredArgsConstructor
public class OtherpageApiController {

    private final UserUseCase userUseCase;

    @GetMapping("/api/otherpage/{id}")
    public ResponseEntity<PageUserResponse> findAllOtherpage(@PathVariable Long id){
        User user = userUseCase.findById(id);
        PageUserResponse pageUserResponse = PageUserResponse.from(user);
        return ResponseEntity.status(200).body(pageUserResponse);
    }

    @PutMapping("/api/otherpage/{id}/follow")
    public ResponseEntity<Void> upfollow(@PathVariable Long id, @AuthenticationPrincipal User user){
        if(userUseCase.upfollwing(user, id)){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(404).build();
    }

    @GetMapping("/api/otherpage/{id}/profile")
    public ResponseEntity<String> viewProfile(@PathVariable Long id) {
        String url  = userUseCase.findById(id).getProfileUrl();

        if(url == null || url.isEmpty()){
            return ResponseEntity.ok().body(null); //나중에 기본 프로필이 보이도록 설정할꺼임
        }
        else {
            return ResponseEntity.ok().body(url);
        }
    }
}
