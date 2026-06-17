package com.kodilla.bungenics.domain.player;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "money")
    private BigDecimal money;

    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    private RabbitFarm rabbitFarm;

}
