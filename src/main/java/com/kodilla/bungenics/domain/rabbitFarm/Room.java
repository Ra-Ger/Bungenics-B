package com.kodilla.bungenics.domain.rabbitFarm;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slots")
    private Integer slots;

    @ManyToOne
    @JoinColumn(name = "structure_id")
    private Structure structure;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    @Builder.Default
    private List<Rabbit> rabbits = new ArrayList<>();
}