// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;

/**
 * All WildAnimal entities have the WildAnimal components. Helps the DeathSystem receive selected BeforeDestroyEvent
 */
public class WildAnimalComponent implements Component {
    /**
     * Name of the icon this WildAnimal has
     */
    public String name;
    /**
     * Name of the icon this WildAnimal has
     */
    @Replicate(value = FieldReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public TextureRegionAsset<?> icon;
}
