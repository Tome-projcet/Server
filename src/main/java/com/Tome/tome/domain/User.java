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
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "lifewriter")
    private String lifewriter;

    @Column(name = "bookcount")
    private int bookcount;

    @Column(name="follow")
    private int follow;

    @Column(name = "follwing")
    private int follwing;

    @Column(name = "first")
    private boolean first = true;

    @Column(name = "profile_url")
    private String profileUrl;

    @Builder
    public User(String email, String password, String nickname, String lifewriter, String auth){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.lifewriter = lifewriter;
        this.bookcount = 0;
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

    public boolean getfirst(){
        return this.first;
    }


    public void updateFirst(){
        this.first = false;
    }
    public User update(String nickname){
        this.nickname = nickname;

        return this;
    }

    public User updateWriter(String writer){
        this.lifewriter = writer;

        return this;
    }

    public void upFollow(){
        this.follow += 1;
    }

    public void upFollowing(){
        this.follwing += 1;
    }

    @Transactional
    public User UpdateProfileUrl(String url){
        this.profileUrl = url;

        return this;
    }
}