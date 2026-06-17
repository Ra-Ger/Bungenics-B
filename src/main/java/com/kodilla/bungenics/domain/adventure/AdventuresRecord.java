package com.kodilla.bungenics.domain.adventure;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
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
@Entity(name = "adventures_records")
public class AdventuresRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "rabbit_id")
    private Rabbit rabbit;

    @OneToMany
    @JoinColumn(name = "adventures")
    private List<Adventure> adventures;
}
