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
package org.terasology.wildAnimals.system;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.wildAnimals.component.WildAnimalComponent;
import org.terasology.wildAnimals.component.WildAnimalGrowthComponent;
import org.terasology.wildAnimals.event.AnimalGrowthEvent;

/**
 * System handling animals changing into other animals.
 *
 * An animal may have multiple prefabs in its lifecycle.  Each prefab
 * except the last should have a {@link WildAnimalGrowthComponent}
 * giving the next prefab in the chain, along with the range of time
 * to spend in this prefab.  At a random time within the specified
 * range (since the growth component was activated, typically
 * immediately on spawning), the existing animal is deleted and
 * replaced with a freshly spawned instance of the new prefab.
 *
 * <hr>
 *
 * <b>Example:</b> Suppose we have a lizard, and we want to model its
 * maturation process as a three-stage cycle, with {@code lizardEgg},
 * {@code babyLizard}, and {@code lizard} prefabs.  We want each lizard
 * to spend 10-15 minutes as an egg, then 5-10 minutes as a baby, before
 * finally maturing.  Then the prefabs should have growth components as
 * follows:
 *
 * lizardEgg.prefab:
 * {@code
 *   "WildAnimalGrowth": {
 *     "minGrowthTime": 600000,
 *     "maxGrowthTime": 900000,
 *     "nextStagePrefab": "WildAnimals:babyLizard"
 *     }
 * }
 *
 * babyLizard.prefab:
 * {@code
 *   "WildAnimalGrowth": {
 *     "minGrowthTime": 300000,
 *     "maxGrowthTime": 600000,
 *     "nextStagePrefab": "WildAnimals:lizard"
 *   }
 * }
 *
 * The final prefab, lizard.prefab, has no growth component.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WildAnimalsGrowthSystem extends BaseComponentSystem {

    /** Unique action ID required by {@code DelayManager.addDelayedAction}. */
    private static final String GROWTH_ID = "WildAnimals:Growth";

    @In
    private DelayManager delayManager;

    @In
    private EntityManager entityManager;

    private Random random = new FastRandom();

    /** Start the growth timer.  Called on activation of the animal's {@code WildAnimalGrowthComponent}. */
    @ReceiveEvent(components = {WildAnimalComponent.class})
    public void onGrowthComponentActivated(OnActivatedComponent event, EntityRef entityRef, WildAnimalGrowthComponent growthComponent) {
      long randomTime = random.nextLong(growthComponent.minGrowthTime, growthComponent.maxGrowthTime);
      delayManager.addDelayedAction(entityRef, GROWTH_ID, randomTime);
    }

    /** Execute the next growth stage.  Called when the growth timer expires. */
    @ReceiveEvent(components = {WildAnimalComponent.class})
    public void onGrowth(DelayedActionTriggeredEvent event, EntityRef entityRef, WildAnimalGrowthComponent growthComponent) {
        LocationComponent locationComponent = entityRef.getComponent(LocationComponent.class);
        entityRef.send(new AnimalGrowthEvent());
        entityRef.destroy();
        entityManager.create(growthComponent.nextStagePrefab, locationComponent.getWorldPosition());
    }
}
