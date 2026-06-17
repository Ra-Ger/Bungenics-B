package com.kodilla.bungenics.domain.rabbit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "secondary_stats")
public class SecondaryStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_weight")
    private Float weight;

    @Column(name = "max_nutrition_level")
    private Float nutritionLevel;

    @Column(name = "max_life")
    private Float life;

    @Column(name = "max_stress")
    private Float stress;

    @Column(name = "max_age")
    private Float age;

    @Column(name = "strength")
    private Float strength;

    @Column(name = "agility")
    private Float agility;

    @Column(name = "intelligence")
    private Float intelligence;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_attack")
    private AttackType preferredAttack;
}
