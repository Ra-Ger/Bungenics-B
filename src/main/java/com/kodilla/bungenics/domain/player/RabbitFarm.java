package com.kodilla.bungenics.domain.player;

import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity(name = "farms")
public class RabbitFarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @OneToMany(mappedBy = "rabbitFarm", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Structure> structures;

    @Column(name = "hay_amount")
    private Float hayAmount;

    @Column(name = "spinach_amount")
    private Float spinachAmount;

    @Column(name = "carrot_amount")
    private Float carrotAmount;

    @Column(name = "lettuce_amount")
    private Float lettuceAmount;

}
