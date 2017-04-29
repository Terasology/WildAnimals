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
package org.terasology.wildAnimals.FleeOnHit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.registry.In;
import org.terasology.wildAnimals.UpdateBehaviorEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class FleeOnHitSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(FleeOnHitSystem.class);

    @In
    private Time time;
    @In
    private AssetManager assetManager;

    /**
     * Updares the FleeOnHitComponent with information about the hit
     */
    @ReceiveEvent(components = FleeOnHitComponent.class)
    public void onDamage(OnDamagedEvent event, EntityRef entity, FleeOnHitComponent fleeOnHitComponent) {
        fleeOnHitComponent.instigator = event.getInstigator();
        fleeOnHitComponent.timeWhenHit = time.getGameTimeInMs();
        entity.saveComponent(fleeOnHitComponent);
        entity.send(new UpdateBehaviorEvent());
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH, components = FleeOnHitComponent.class)
    public void onUpdateBehavior(UpdateBehaviorEvent event, EntityRef entity, FleeOnHitComponent fleeOnHitComponent) {
        if (fleeOnHitComponent.instigator != null) {
            // Start fleeing behavior, when a hit that is recorded is recent
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            behaviorComponent.tree = assetManager.getAsset("WildAnimals:flee", BehaviorTree.class).get();
            logger.info("Changed behavior to Flee");
            // Increase speed by multiplier factor
            CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
            characterMovementComponent.speedMultiplier = fleeOnHitComponent.speedMultiplier;
            entity.saveComponent(characterMovementComponent);
            //TODO: add a onBehaviorChanged method to BehaviorSystem that receives a OnChangedComponent Event
            //entity.saveComponent(behaviorComponent);
            entity.removeComponent(BehaviorComponent.class);
            entity.addComponent(behaviorComponent);

        } else {
            // To stop 'flee'ing behavior
            fleeOnHitComponent.instigator = null;
            entity.saveComponent(fleeOnHitComponent);
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            // Restore speed to normal
            CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
            characterMovementComponent.speedMultiplier = fleeOnHitComponent.defaultSpeedMultipler;
            entity.saveComponent(characterMovementComponent);
            // Change behavior to "stray"
            behaviorComponent.tree = assetManager.getAsset("Pathfinding:stray", BehaviorTree.class).get();
            logger.info("Changed behavior to stray");
            entity.removeComponent(BehaviorComponent.class);
            entity.addComponent(behaviorComponent);
        }
    }
}
