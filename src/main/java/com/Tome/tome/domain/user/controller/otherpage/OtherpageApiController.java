package com.Tome.tome.domain.user.controller.otherpage;

import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "OtherPage", description = "다른 사용자 페이지 API")
@Controller
@RequiredArgsConstructor
public class OtherpageApiController {

    private final UserUseCase userUseCase;

    @Operation(summary = "다른 사용자 정보 조회")
    @GetMapping("/api/otherpage/{id}")
    public ResponseEntity<PageUserResponse> findAllOtherpage(@PathVariable Long id){
        User user = userUseCase.findById(id);
        PageUserResponse pageUserResponse = PageUserResponse.from(user);
        return ResponseEntity.status(200).body(pageUserResponse);
    }

    @Operation(summary = "팔로우 / 언팔로우", description = "이미 팔로우 중이면 언팔로우합니다. 자기 자신이면 400을 반환합니다.")
    @PutMapping("/api/otherpage/{id}/follow")
    public ResponseEntity<Void> upfollow(@PathVariable Long id, @AuthenticationPrincipal User user){
        userUseCase.upfollwing(user, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "다른 사용자 프로필 이미지 URL 조회")
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
