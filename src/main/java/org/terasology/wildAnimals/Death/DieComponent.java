// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.Death;

import com.google.common.collect.Lists;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class DieComponent implements Component<DieComponent> {
    /**
     * A pool of death animations. It gets currently only used by behavior trees to make a skeletal mesh perform an
     * animation while dying/falling. The animations of the pool will be played once.
     */
    public List<MeshAnimation> animationPool = Lists.newArrayList();
    public List<String> itemsDropped = Lists.newArrayList();

    @Override
    public void copyFrom(DieComponent other) {
        this.animationPool.clear();
        this.animationPool.addAll(other.animationPool);
        this.itemsDropped.clear();
        this.itemsDropped.addAll(other.itemsDropped);
    }
}
