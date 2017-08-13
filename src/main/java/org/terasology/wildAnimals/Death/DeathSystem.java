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
package org.terasology.wildAnimals.Death;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.health.BeforeDestroyEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.registry.In;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.wildAnimals.component.WildAnimalComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class DeathSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(DeathSystem.class);

    @In
    EntityManager entityManager;
    @In
    private Time time;

    /**
     * On every update, checks for entities which have DestroyAtAnimationEndComponent,
     * finds them and destroys them.
     * Sending the DoDestroyEvent is essential for the BlockDropGrammar system to handle
     * item drops specified in the animal's DieComponent
     */
    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        for (EntityRef entity : entityManager.getEntitiesWith(DestroyAtAnimationEndComponent.class)) {
            DestroyAtAnimationEndComponent destroyAtAnimationEndComponent = entity.getComponent(DestroyAtAnimationEndComponent.class);
            if (destroyAtAnimationEndComponent.deathTime < currentTime) {
                entity.send(new DoDestroyEvent(destroyAtAnimationEndComponent.getInstigator(), destroyAtAnimationEndComponent.getDirectCause(), destroyAtAnimationEndComponent.getDamageType()));
                entity.destroy();
            }
        }
    }

    /**
     * Compute and save deathTime whenever a DestroyAtAnimationEndComponent is added
     */
    @ReceiveEvent
    public void addedDestroyAtAnimationEndComponent(OnAddedComponent event, EntityRef entityRef, DestroyAtAnimationEndComponent destroyAtAnimationEndComponent) {
        destroyAtAnimationEndComponent.deathTime = time.getGameTimeInMs() + (long) (destroyAtAnimationEndComponent.lifespan * 1000);
        entityRef.saveComponent(destroyAtAnimationEndComponent);
    }

    /**
     * Receives and consumes the BeforeDestroyEvent.
     * Removes extra components from the animal entity and updates skeletalMesh to play dying animation
     * Triggers the entity to self destruct after animation ends by attaching DestroyAtAnimationEndComponent
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH, components = {WildAnimalComponent.class, DieComponent.class})
    public void onDeath(BeforeDestroyEvent event, EntityRef entity, DieComponent dieComponent) {
        event.consume();
        entity.removeComponent(BehaviorComponent.class);
        entity.removeComponent(CharacterMovementComponent.class);
        SkeletalMeshComponent skeletalMeshComponent = entity.getComponent(SkeletalMeshComponent.class);
        if (skeletalMeshComponent == null) {
            return;
        }
        // Add fall animation from DieComponent
        skeletalMeshComponent.animation = null;
        skeletalMeshComponent.animationPool.clear();
        skeletalMeshComponent.animationPool.addAll(dieComponent.animationPool);
        skeletalMeshComponent.loop = false;
        entity.saveComponent(skeletalMeshComponent);
        // Find total length of animations
        float lifespan = 0;
        for (MeshAnimation meshAnimation : skeletalMeshComponent.animationPool) {
            lifespan += meshAnimation.getTimePerFrame() * (meshAnimation.getFrameCount() - 1);
        }
        // Trigger entity to self destruct after animations end
        DestroyAtAnimationEndComponent destroyAtAnimationEndComponent = new DestroyAtAnimationEndComponent(lifespan, event.getInstigator(), event.getDirectCause(), event.getDamageType());
        entity.addOrSaveComponent(destroyAtAnimationEndComponent);
    }
}
