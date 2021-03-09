// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.component;

import org.terasology.engine.entitySystem.Component;

/**
 * This components allows WildAnimals to grow into their next stage.
 */
public class WildAnimalGrowthComponent implements Component {
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
}
