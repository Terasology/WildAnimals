/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.wildAnimals.component;

import org.terasology.entitySystem.Component;

/**
 * This component allows WildAnimals to grow into their next stage.
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
