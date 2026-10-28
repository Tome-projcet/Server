package com.Tome.tome.domain.user.controller.user;

import com.Tome.tome.domain.user.dto.AddUserRequest;
import com.Tome.tome.domain.user.dto.NicknameRequest;
import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.user.dto.WriterRequest;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "User", description = "회원가입 / 온보딩 API")
@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserUseCase userUseCase;

    @Operation(summary = "회원가입", description = "JSON으로 email, password(8자 이상), nickname을 받습니다. X-XSRF-TOKEN 헤더가 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 성공, 생성된 사용자 id 반환"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 / 이메일 존재 / 닉네임 존재")
    })
    @SecurityRequirements
    @PostMapping("/user")
    public ResponseEntity<Map<String, Long>> signup(@RequestBody AddUserRequest request){
        Long userId = userUseCase.save(request);
        return ResponseEntity.status(201).body(Map.of("id", userId));
    }

    @Operation(summary = "첫 로그인 여부 확인", description = "온보딩 닉네임을 설정하기 전까지 firstLogin이 true입니다. 조회만 하고 값을 바꾸지 않습니다.")
    @GetMapping("/api/main")
    public ResponseEntity<Map<String, Boolean>> main(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(Map.of("firstLogin", userUseCase.isFirstLogin(user.getId())));
    }

    @Operation(summary = "온보딩 닉네임 설정", description = "성공하면 첫 로그인 상태가 해제됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 성공"),
            @ApiResponse(responseCode = "400", description = "이름이 비어있음 / 동일한 닉네임 / 이미 사용중인 닉네임")
    })
    @PutMapping("/api/onbording/nickname")
    public ResponseEntity<PageUserResponse> setting(@AuthenticationPrincipal User user, @RequestBody NicknameRequest request){
        User updateuser = userUseCase.updateNickname(user.getId(), request.nickname());
        userUseCase.completeFirstLogin(user.getId());

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }

    @Operation(summary = "온보딩 인생 작가 설정")
    @PutMapping("/api/onbording/writer")
    public ResponseEntity<PageUserResponse> settingwriter(@AuthenticationPrincipal User user, @RequestBody WriterRequest request){
        User updateuser = userUseCase.updateWriter(user.getId(), request.writer());

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }
}
