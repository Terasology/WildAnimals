// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.Death;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.animation.MeshAnimation;

import java.util.List;

public class DieComponent implements Component {
    /**
     * A pool of death animations. It gets currently only used by behavior trees to make a skeletal mesh perform an
     * animation while dying/falling. The animations of the pool will be played once.
     */
    public List<MeshAnimation> animationPool = Lists.newArrayList();
    public List<String> itemsDropped = Lists.newArrayList();
}
