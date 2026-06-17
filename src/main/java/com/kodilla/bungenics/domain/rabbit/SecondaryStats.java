package com.kodilla.bungenics.domain.rabbit;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
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

    @Column(name = "basic_strength")
    private Float basicStrength;

    @Column(name = "basic_agility")
    private Float basicAgility;

    @Column(name = "basic_intelligence")
    private Float basicIntelligence;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_attack")
    private AttackType preferredAttack;
}