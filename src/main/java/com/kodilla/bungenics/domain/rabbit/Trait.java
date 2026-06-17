package com.kodilla.bungenics.domain.rabbit;

import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "traits")
public class Trait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rabbit_id")
    private Rabbit rabbit;

    @Enumerated(EnumType.STRING)
    @Column(name = "trait_type")
    private TraitType traitType;
}
