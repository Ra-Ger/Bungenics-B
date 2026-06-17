package com.kodilla.bungenics.domain.rabbitFarm;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    @JoinColumn(name = "rabbits")
    private List<Rabbit> rabbits;

    @Column(name = "slots")
    private Integer slots;
}
