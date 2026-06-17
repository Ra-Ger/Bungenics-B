package com.kodilla.bungenics.domain.adventure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adventures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Adventure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "rabbit_id", nullable = false)
    private Long rabbitId;

    @Column(name = "adventure_type")
    private String type;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adventures_record_id")
    @JsonIgnore
    private AdventuresRecord adventuresRecord;

    @OneToMany(
            mappedBy = "adventure",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<AdventureEvent> adventureEvents = new ArrayList<>();

    @Transient
    @JsonIgnore
    public void addEvent(AdventureEvent event) {
        if (event != null) {
            adventureEvents.add(event);
            event.setAdventure(this);
        }
    }

    @Transient
    @JsonIgnore
    public void removeEvent(AdventureEvent event) {
        if (event != null) {
            adventureEvents.remove(event);
            event.setAdventure(null);
        }
    }
}