package com.kodilla.bungenics.domain.rabbitFarm;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity(name = "structures")
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "farm_id")
    private RabbitFarm rabbitFarm;

    @Column(name = "rabbit_farm_id")
    private Long rabbitFarmId;

    @Enumerated(EnumType.STRING)
    @Column(name = "structure_type")
    private StructureType structureType;

    @Column(name = "grid_index")
    private Integer gridIndex;

    @Column(name = "slots")
    private Integer slots;

    @OneToMany(mappedBy = "structure", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();
}