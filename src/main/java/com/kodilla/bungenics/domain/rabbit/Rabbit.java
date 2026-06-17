package com.kodilla.bungenics.domain.rabbit;

import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
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
@Entity(name = "rabbits")
public class Rabbit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "breed")
    private Breed breed;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "nutrition_level")
    private Float nutritionLevel;

    @Column(name = "life")
    private Float life;

    @Column(name = "stress")
    private Float stress;

    @Column(name = "age")
    private Float age;

    @Column(name = "mother_id")
    private Long mother;

    @Column(name = "father_id")
    private Long father;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "rabbit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Trait> traits;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "secondary_stats")
    private SecondaryStats secondaryStats;
}


