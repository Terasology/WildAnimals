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
package org.terasology.wildAnimals.FindNearbyEntities;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.wildAnimals.UpdateBehaviorEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class FindNearbyEntitiesSystem extends BaseComponentSystem implements UpdateSubscriberSystem {


    private static final Logger logger = LoggerFactory.getLogger(FindNearbyEntitiesSystem.class);

    @In
    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        // Iterate through all the Breaking blocks
        for (EntityRef entity : entityManager.getEntitiesWith(FindNearbyEntitiesComponent.class)) {
            Vector3f actorPosition = entity.getComponent(LocationComponent.class).getWorldPosition();
            FindNearbyEntitiesComponent findNearbyEntitiesComponent = entity.getComponent(FindNearbyEntitiesComponent.class);
            float maxDistance = findNearbyEntitiesComponent.searchRadius;
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

            if (!isEqual(charactersWithinRange, findNearbyEntitiesComponent.charactersWithinRange)) {
                findNearbyEntitiesComponent.charactersWithinRange = charactersWithinRange;
                entity.saveComponent(findNearbyEntitiesComponent);
                entity.send(new UpdateBehaviorEvent());
            }
        }
    }

    private boolean isEqual(List<EntityRef> one, List<EntityRef> two) {
        if((one == null && two != null) || (one != null && two == null)){
            return false;
        }
        if (one.size() != two.size()){
            return false;
        }
        final Set<EntityRef> s1 = new HashSet<>(one);
        final Set<EntityRef> s2 = new HashSet<>(two);
        return s1.equals(s2);
    }
}
