package com.Tome.tome.domain.user.controller;

import com.Tome.tome.domain.user.dto.AddUserRequest;
import com.Tome.tome.domain.user.dto.PageUserResponse;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final UserUseCase userUseCase;

    @PostMapping("/user")
    public String signup(AddUserRequest request, RedirectAttributes redirectAttributes){
        try {
            userUseCase.save(request);
        } catch (IllegalArgumentException e) {
            if(e.getMessage().equals("조건이 틀렸습니다")) {
                return "redirect:/signup?error";
            }
            else{
                redirectAttributes.addFlashAttribute("error1", e.getMessage());
                return "redirect:/signup?error1";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/api/main")
    public String main(@AuthenticationPrincipal User user){
        if(userUseCase.checkFirstLogin(user.getId())){
            return "redirect:/onbording/nickname";
        }
        else{
            return "redirect:/main";
        }
    }

    @PutMapping("/api/onbording/nickname")
    public ResponseEntity<PageUserResponse> setting(@AuthenticationPrincipal User user, String nickname){
        User updateuser = userUseCase.updateNickname(user.getId(), nickname);

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }

    @PutMapping("/api/onbording/writer")
    public ResponseEntity<PageUserResponse> settingwriter(@AuthenticationPrincipal User user, String writer){
        User updateuser = userUseCase.updateWriter(user.getId(), writer);

        return ResponseEntity.ok().body(PageUserResponse.from(updateuser));
    }
}
