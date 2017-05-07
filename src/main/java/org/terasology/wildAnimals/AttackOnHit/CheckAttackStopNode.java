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
package org.terasology.wildAnimals.AttackOnHit;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.pathfinding.components.FollowComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.wildAnimals.FleeOnHit.FleeOnHitComponent;
import org.terasology.wildAnimals.UpdateBehaviorEvent;

import java.util.List;

/**
 * Makes the character follow a player within a given range
 * Sends FAILURE when the distance is greater than maxDistance
 * along with an UpdateBehaviorEvent
 */
public class CheckAttackStopNode extends Node {

    @Override
    public CheckAttackStopTask createTask() {
        return new CheckAttackStopTask(this);
    }

    public static class CheckAttackStopTask extends Task {

        @In
        private EntityManager entityManager;

        public CheckAttackStopTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            Status status = getStatusWithoutReturn();
            if (status == Status.FAILURE) {
                AttackOnHitComponent attackOnHitComponent = this.actor().getComponent(AttackOnHitComponent.class);
                attackOnHitComponent.instigator = null;
                this.actor().getEntity().saveComponent(attackOnHitComponent);
                this.actor().getEntity().removeComponent(FollowComponent.class);
                this.actor().getEntity().send(new UpdateBehaviorEvent());
            }
            return status;
        }

        private Status getStatusWithoutReturn() {
            LocationComponent actorLocationComponent = actor().getComponent(LocationComponent.class);
            if (actorLocationComponent == null) {
                return Status.FAILURE;
            }
            Vector3f actorPosition = actorLocationComponent.getWorldPosition();
            float maxDistance = this.actor().getComponent(AttackOnHitComponent.class).maxDistance;

            float maxDistanceSquared = maxDistance*maxDistance;
            FollowComponent followWish = actor().getComponent(FollowComponent.class);
            if (followWish == null || followWish.entityToFollow == null) {
                return Status.FAILURE;
            }

            LocationComponent locationComponent = followWish.entityToFollow.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                return Status.FAILURE;
            }
            if (locationComponent.getWorldPosition().distanceSquared(actorPosition) <= maxDistanceSquared) {
                return Status.RUNNING;
            }
           return Status.FAILURE;
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public CheckAttackStopNode getNode() {
            return (CheckAttackStopNode) super.getNode();
        }
    }
}
