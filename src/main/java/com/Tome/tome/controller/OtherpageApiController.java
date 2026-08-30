package com.Tome.tome.controller;


import com.Tome.tome.domain.User;
import com.Tome.tome.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class OtherpageApiController {

    private UserRepository userRepository;

    @GetMapping("/api/otherpage/{id}")
    public ResponseEntity<User> findAllOtherpage(@PathVariable Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found " + id));

        return ResponseEntity.status(200).body(user);
    }


}