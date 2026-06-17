package com.kodilla.bungenics.domain.adventure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "adventure_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdventureEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "result", length = 1000)
    private String result;

    @Column(name = "gold_reward", precision = 12, scale = 2)
    private BigDecimal goldReward;

    @Column(name = "carrot_reward")
    private Float carrotReward;

    @Column(name = "lettuce_reward")
    private Float lettuceReward;

    @Column(name = "spinach_reward")
    private Float spinachReward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adventure_id")
    @JsonIgnore
    private Adventure adventure;
}