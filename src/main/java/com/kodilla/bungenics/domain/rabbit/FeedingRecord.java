package com.kodilla.bungenics.domain.rabbit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity(name = "feeding_records")
public class FeedingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rabbit_id")
    private Rabbit rabbit;

    @Column(name = "food_type")
    private String foodType;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "feeding_time")
    private LocalDateTime feedingTime;
}
