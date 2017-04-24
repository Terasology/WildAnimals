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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.health.BeforeDestroyEvent;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.wildAnimals.component.WildAnimalComponent;

import java.util.List;


@RegisterSystem(RegisterMode.AUTHORITY)
public class DeathSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(DeathSystem.class);

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH, components = {WildAnimalComponent.class, DieComponent.class})
    public void onDeath(BeforeDestroyEvent event, EntityRef entity, DieComponent dieComponent) {
        logger.info("dead");
        event.consume();
        entity.removeComponent(BehaviorComponent.class);
        entity.removeComponent(CharacterMovementComponent.class);
        SkeletalMeshComponent skeletalMeshComponent = entity.getComponent(SkeletalMeshComponent.class);
        if (skeletalMeshComponent == null)  {
            return;
        }
        List<MeshAnimation> wantedAnimationPool;
        wantedAnimationPool = dieComponent.animationPool;
        skeletalMeshComponent.animation = null;
        skeletalMeshComponent.animationPool.clear();
        skeletalMeshComponent.animationPool.addAll(wantedAnimationPool);
        skeletalMeshComponent.loop = false;
        entity.saveComponent(skeletalMeshComponent);
        float lifespan = 0;
        for (MeshAnimation meshAnimation : skeletalMeshComponent.animationPool) {

            logger.info("time per frame: " + meshAnimation.getTimePerFrame() + " frame count: " + (meshAnimation.getFrameCount() - 1));
            lifespan += meshAnimation.getTimePerFrame() * (meshAnimation.getFrameCount() - 1);
        }
        LifespanComponent lifespanComponent = new LifespanComponent(lifespan);
        entity.addComponent(lifespanComponent);
    }
}
