// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This components allows WildAnimals to grow into their next stage.
 */
public class WildAnimalGrowthComponent implements Component<WildAnimalGrowthComponent> {
    /**
     * The minimum time the animal will stay in this stage, in milliseconds.
     */
    public long minGrowthTime;

    /**
     * The maximum time the animal will stay in this stage, in milliseconds.
     */
    public long maxGrowthTime;

    /**
     * The prefab for the next stage the animal will grow into.
     */
    public String nextStagePrefab;

    @Override
    public void copy(WildAnimalGrowthComponent other) {
        this.minGrowthTime = other.minGrowthTime;
        this.maxGrowthTime = other.maxGrowthTime;
        this.nextStagePrefab = other.nextStagePrefab;
    }
}
