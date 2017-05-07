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
package org.terasology.wildAnimals.AttackInProximity;

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
import org.terasology.wildAnimals.UpdateBehaviorEvent;

import java.util.List;

/**
 * Makes the character follow and damage any player that enters a given range
 * Sends FAILURE when the distance is greater than maxDistance
 * along with an UpdateBehaviorEvent
 */
public class CheckProximityAttackStopNode extends Node {

    @Override
    public CheckProximityAttackStopTask createTask() {
        return new CheckProximityAttackStopTask(this);
    }

    public static class CheckProximityAttackStopTask extends Task {

        @In
        private EntityManager entityManager;

        public CheckProximityAttackStopTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            Status status = getStatusWithoutReturn();
            if (status == Status.FAILURE) {
                AttackInProximityComponent attackInProximityComponent = this.actor().getComponent(AttackInProximityComponent.class);
                attackInProximityComponent.nearbyEntity = null;
                this.actor().getEntity().saveComponent(attackInProximityComponent);
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

            float maxDistance = this.actor().getComponent(AttackInProximityComponent.class).maxDistance;
            float maxDistanceSquared = maxDistance*maxDistance;

            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            List<EntityRef> charactersWithinRange = Lists.newArrayList();
            for (EntityRef client: clients) {
                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                EntityRef character = clientComponent.character;
                LocationComponent locationComponent = character.getComponent(LocationComponent.class);
                if (locationComponent == null) {
                    continue;
                }
                if (locationComponent.getWorldPosition().distanceSquared(actorPosition) <= maxDistanceSquared) {
                    charactersWithinRange.add(character);
                }
            }

            if (charactersWithinRange.isEmpty()) {
                return Status.FAILURE;
            }

            // TODO select closest character
            EntityRef someCharacterWithinRange = charactersWithinRange.get(0);
            FollowComponent followComponent = new FollowComponent();
            followComponent.entityToFollow = someCharacterWithinRange;
            this.actor().getEntity().addOrSaveComponent(followComponent);
            AttackInProximityComponent attackInProximityComponent = actor().getComponent(AttackInProximityComponent.class);
            attackInProximityComponent.nearbyEntity = someCharacterWithinRange;
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public CheckProximityAttackStopNode getNode() {
            return (CheckProximityAttackStopNode) super.getNode();
        }
    }
}
