package com.Tome.tome.controller;


import com.Tome.tome.domain.User;
import com.Tome.tome.repository.UserRepository;
import com.Tome.tome.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class OtherpageApiController {

    private UserRepository userRepository;
    protected UserService userService;

    @GetMapping("/api/otherpage/{id}")
    public ResponseEntity<User> findAllOtherpage(@PathVariable Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));

        return ResponseEntity.status(200).body(user);
    }


    @Transactional
    @GetMapping("/api/otherpage/{id}/follow")
    public ResponseEntity<Void> upfollow(@PathVariable Long id, @AuthenticationPrincipal User user){
        userService.upfollwing(user, id);

        return ResponseEntity.ok().build();
    }

}