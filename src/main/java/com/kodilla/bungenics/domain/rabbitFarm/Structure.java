package com.kodilla.bungenics.domain.rabbitFarm;

import com.kodilla.bungenics.domain.player.RabbitFarm;
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
@Entity(name = "structures")
public class Structure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rabbit_farm_id")
    private RabbitFarm rabbitFarm;


    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "structure_id") 
    private List<Room> rooms;

    @Column(name = "slots")
    private Integer slots;

    @Enumerated(EnumType.STRING)
    @Column(name = "structure_type")
    private StructureType structureType;
}
