package com.Tome.tome.domain.user.controller.mypage;

import com.Tome.tome.domain.genre.dto.GenreRequest;
import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.port.in.UserMostUseCase;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import com.Tome.tome.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MyPage", description = "마이페이지 API")
@RequiredArgsConstructor
@Controller
public class MypageApiController {

    private final UserUseCase userUseCase;
    private final UserMostUseCase userMostUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/api/mypage")
    public ResponseEntity<PageUserResponse> viewmypage(@AuthenticationPrincipal User user){
        User user1 = userUseCase.findById(user.getId());
        PageUserResponse pageUserResponse = PageUserResponse.from(user1);
        return ResponseEntity.status(200).body(pageUserResponse);
    }

    @Operation(summary = "프로필 이미지 URL 설정", description = "url은 쿼리 파라미터로 받습니다. http:// 또는 https://로 시작해야 합니다.")
    @PutMapping("/api/mypage/profile")
    public ResponseEntity<PageUserResponse> settingProfile(@AuthenticationPrincipal User user, String url){
        User updateuser = userUseCase.updateProfileUrl(user.getId(), url);

        return ResponseEntity.status(201).body(PageUserResponse.from(updateuser));
    }

    @Operation(summary = "내 프로필 이미지 URL 조회")
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

    @Operation(summary = "닉네임 변경", description = "nickname은 쿼리 파라미터로 받습니다.")
    @PutMapping("/api/mypage/update/nickname")
    public ResponseEntity<PageUserResponse> updateNickname(@AuthenticationPrincipal User user, String nickname){
            User updateuser = userUseCase.updateNickname(user.getId(), nickname);
            return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }

    @Operation(summary = "프로필 이미지 URL 변경", description = "url은 쿼리 파라미터로 받습니다.")
    @PutMapping("/api/mypage/update/profile")
    public ResponseEntity<PageUserResponse> updateProfile(@AuthenticationPrincipal User user, String url){
        User updateuser = userUseCase.updateProfileUrl(user.getId(), url);

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }

    @Operation(summary = "인생 작가 변경", description = "writer는 쿼리 파라미터로 받습니다.")
    @PutMapping("/api/mypage/update/writer")
    public ResponseEntity<PageUserResponse> updateWriter(@AuthenticationPrincipal User user, String writer){
        User updateuser = userUseCase.updateWriter(user.getId(), writer);

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }

    @Operation(summary = "온보딩 선호 장르 선택")
    @PostMapping("/api/onbording/genre")
    public ResponseEntity<Void> selectGenre(@AuthenticationPrincipal User user, @RequestBody GenreRequest request){
        userUseCase.checkGenre(user, request.getName());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "선호 장르 TOP3 갱신")
    @PutMapping("/api/mypage/mostgenre")
    public ResponseEntity<Void> mostgenre(@AuthenticationPrincipal User user){
        userMostUseCase.mostGenreset(user);

        return ResponseEntity.status(200).build();
    }
}
