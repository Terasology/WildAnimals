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
package org.terasology.wildAnimals;

import org.mockito.cglib.beans.BeanGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.health.BeforeDestroyEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.wildAnimals.component.DieComponent;
import org.terasology.wildAnimals.component.FleeComponent;
import org.terasology.wildAnimals.component.WildAnimalComponent;


@RegisterSystem(RegisterMode.AUTHORITY)
public class DeathSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(DeathSystem.class);

    @ReceiveEvent(components = WildAnimalComponent.class)
    public void onDamage(OnDamagedEvent event, EntityRef entity) {

        HealthComponent healthComponent = entity.getComponent(HealthComponent.class);
        logger.info("remaining " + healthComponent.currentHealth);
        if (!entity.hasComponent(FleeComponent.class)) {
            FleeComponent fleeComponent = new FleeComponent();
            entity.addComponent(fleeComponent);
        }
    }

    @ReceiveEvent(components = WildAnimalComponent.class)
    public void onDeath(BeforeDestroyEvent event, EntityRef entity) {
        logger.info("dead");
    }
}
