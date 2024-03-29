// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.Death;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.health.BeforeDestroyEvent;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.engine.rendering.logic.SkeletalMeshComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
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
     * Sending the DoDestroyEvent is essential for the DropGrammar system to handle
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
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = {WildAnimalComponent.class, DieComponent.class})
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
