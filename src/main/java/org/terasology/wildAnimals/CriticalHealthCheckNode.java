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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.registry.In;
import org.terasology.wildAnimals.component.FleeComponent;

public class CriticalHealthCheckNode extends Node {
    private static final Logger logger = LoggerFactory.getLogger(CriticalHealthCheckNode.class);

    public CriticalHealthCheckNode() {
    }

    public CriticalHealthCheckTask createTask() {
        return new CriticalHealthCheckTask(this);
    }

    public static class CriticalHealthCheckTask extends Task {

        public CriticalHealthCheckTask(Node node) {
            super(node);
        }

        public Status update(float dt) {
            HealthComponent healthComponent = this.actor().getComponent(HealthComponent.class);
            if (healthComponent.currentHealth > 5) {
                logger.info("not critical");
                return Status.SUCCESS;
            } else {
                logger.info("health is critical");
                return Status.FAILURE;
            }
        }

        public void handle(Status result) {
        }

        public CriticalHealthCheckNode getNode() {
            return (CriticalHealthCheckNode) super.getNode();
        }
    }
}
