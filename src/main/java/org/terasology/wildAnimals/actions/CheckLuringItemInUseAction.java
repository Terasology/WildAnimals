// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.wildAnimals.actions;


import org.terasology.behaviors.components.FindNearbyPlayersComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.items.BlockItemComponent;

/**
 * Behavior node that checks if the current held item can be used to lure
 */

@BehaviorAction(name = "check_luring_item_in_use")
public class CheckLuringItemInUseAction extends BaseAction {

    static final String[] LURING_ITEMS = {
            "CoreAssets:TallGrass1",
            "CoreAssets:TallGrass2",
            "CoreAssets:TallGrass3",
            "CoreAssets:Lavender",
            "CoreAssets:Dandelion"
    };

    @Override
    public BehaviorState modify(Actor actor, BehaviorState behaviorState) {
        FindNearbyPlayersComponent component = actor.getComponent(FindNearbyPlayersComponent.class);
        EntityRef player = component.closestCharacter;
        CharacterHeldItemComponent characterHeldItemComponent = player.getComponent(CharacterHeldItemComponent.class);
        EntityRef heldItem = characterHeldItemComponent.selectedItem;
        BlockItemComponent blockItemComponent = heldItem.getComponent(BlockItemComponent.class);
        for (String item : LURING_ITEMS) {
            if (blockItemComponent != null && blockItemComponent.blockFamily.getURI().equals(new BlockUri(item))) {
                return BehaviorState.SUCCESS;
            }
        }
        return BehaviorState.FAILURE;
    }
}

