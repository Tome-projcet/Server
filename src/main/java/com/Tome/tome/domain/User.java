package com.Tome.tome.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private String nickname;

    @Column
    private String lifewriter;

    @Column
    private String bookcount;

    @Column
    private int follow;

    @Column
    private int follwing;


    @Column(name = "profile_url")
    private String profileUrl;

    @Builder
    public User(String email, String password, String nickname, String lifewriter, String auth){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.lifewriter = lifewriter;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override
    public String getUsername(){
        return email;
    }

    public String getmail(){
        return email;
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    public User update(String nickname){
        this.nickname = nickname;

        return this;
    }

    public void upFollow(){
        this.follow += 1;
    }

    public void upFollowing(){
        this.follwing += 1;
    }

    @Transactional
    public void setProfileUrl(String url){
        this.profileUrl = url;
    }
}