package com.kodilla.bungenics.domain.adventure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adventures_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdventuresRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rabbit_id", unique = true)
    private Rabbit rabbit;

    @OneToMany(
            mappedBy = "adventuresRecord",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Adventure> adventures = new ArrayList<>();

    public void addAdventure(Adventure adventure) {
        if (adventure != null) {
            adventures.add(adventure);
            adventure.setAdventuresRecord(this);
        }
    }

    public void removeAdventure(Adventure adventure) {
        if (adventure != null) {
            adventures.remove(adventure);
            adventure.setAdventuresRecord(null);
        }
    }
}