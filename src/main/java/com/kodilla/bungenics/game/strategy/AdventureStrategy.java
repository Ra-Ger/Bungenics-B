package com.kodilla.bungenics.game.strategy;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.domain.rabbit.Rabbit;

import java.util.List;

public interface AdventureStrategy {
    String getAdventureType();
    List<AdventureEvent> executeAdventure(Rabbit rabbit);
}