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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.network.Replicate;
import org.terasology.protobuf.EntityData;

public class DestroyAtAnimationEndComponent implements Component {

    // Lifespan in seconds
    @Replicate
    public float lifespan = 5;
    @Replicate
    public long deathTime;

    private EntityRef instigator;
    private EntityRef directCause;
    private Prefab damageType;

    public DestroyAtAnimationEndComponent() {
    }

    public DestroyAtAnimationEndComponent(float span) {
        this.lifespan = span;
    }

    public DestroyAtAnimationEndComponent(float span, EntityRef instigator, EntityRef directCause, Prefab damageType) {
        this.lifespan = span;
        this.instigator = instigator;
        this.directCause = directCause;
        this.damageType = damageType;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }

    public Prefab getDamageType() {
        return damageType;
    }
}
