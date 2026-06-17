package com.kodilla.bungenics.domain.rabbit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "RABBITS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rabbit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long playerId;
    private String name;
    private String breed;
    private String sex;
    private Float weight;
    private Float adultWeight;
    private Float nutritionLevel;
    private Float life;
    private Float stress;
    private Float age;

    @Builder.Default
    private Float maxLifetime = 16.0f;

    private Long motherId;
    private Long fatherId;

    @Enumerated(EnumType.STRING)
    private RabbitStatus status;

    private LocalDateTime breedingEndTime;
    private LocalDateTime adventureEndTime;
    private LocalDateTime vetEndTime;
    private LocalDateTime trainingEndTime;
    private LocalDateTime restEndTime;

    private String trainingEnhancedFood;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "secondary_stats_id")
    private SecondaryStats secondaryStats;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "RABBIT_TRAITS", joinColumns = @JoinColumn(name = "rabbit_id"))
    @Column(name = "trait")
    @Builder.Default
    private Set<RabbitTrait> traits = new HashSet<>();

    @Transient
    @JsonIgnore
    public boolean hasTrait(RabbitTrait trait) {
        return traits != null && traits.contains(trait);
    }

    @Transient
    @JsonIgnore
    public float getMaxHp() {
        if (secondaryStats != null && secondaryStats.getLife() != null) {
            return secondaryStats.getLife();
        }
        if (breed != null) {
            return Breed.parseOrDefault(breed).getMaxLife();
        }
        return 100.0f;
    }

    @Transient
    @JsonIgnore
    public float getMaxStress() {
        if (secondaryStats != null && secondaryStats.getStress() != null) {
            return secondaryStats.getStress();
        }
        if (breed != null) {
            return Breed.parseOrDefault(breed).getMaxStress();
        }
        return 100.0f;
    }
}