package com.Tome.tome.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_favorit")
@Setter
public class UserMostGenre {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @OneToOne
    @JoinColumn
    private User user;

    @Column
    private String first;

    @Column
    private String second;

    @Column
    private String third;


    public void setFirst(String first){
        this.first = first;
    }

    public void setSecond(String second){
        this.second = second;
    }

    public void setThird(String third){
        this.third = third;
    }
}
