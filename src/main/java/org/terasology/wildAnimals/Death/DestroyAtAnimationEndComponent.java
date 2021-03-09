// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.Death;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.Replicate;

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
