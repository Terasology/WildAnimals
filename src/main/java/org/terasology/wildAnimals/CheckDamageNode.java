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
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.registry.In;
import org.terasology.wildAnimals.component.FleeComponent;

public class CheckDamageNode extends Node {
    private static final Logger logger = LoggerFactory.getLogger(CheckDamageNode.class);

    public CheckDamageNode() {
    }

    public DamageFollowedPlayerTask createTask() {
        return new DamageFollowedPlayerTask(this);
    }

    public static class DamageFollowedPlayerTask extends Task {

        public DamageFollowedPlayerTask(Node node) {
            super(node);
        }

        public Status update(float dt) {
//            HealthComponent healthComponent = (HealthComponent)this.actor().getComponent(HealthComponent.class);
            FleeComponent fleeComponent = (FleeComponent) this.actor().getComponent(FleeComponent.class);
            if (fleeComponent != null) {
                logger.info("Damage done");
                this.actor().getEntity().removeComponent(FleeComponent.class);
                return Status.FAILURE;
            } else {
                return Status.RUNNING;
            }
        }

        public void handle(Status result) {
        }

        public CheckDamageNode getNode() {
            return (CheckDamageNode) super.getNode();
        }
    }
}
