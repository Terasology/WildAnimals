// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.wildAnimals.actions;


import org.terasology.module.behaviors.components.FindNearbyPlayersComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.module.behaviors.components.LuringComponent;

/**
 * Behavior node that checks if the current held item can be used to lure
 */

@BehaviorAction(name = "check_luring_item_in_use")
public class CheckLuringItemInUseAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState behaviorState) {
        FindNearbyPlayersComponent component = actor.getComponent(FindNearbyPlayersComponent.class);
        LuringComponent lure = actor.getComponent(LuringComponent.class);

        EntityRef player = component.closestCharacter;

        CharacterHeldItemComponent characterHeldItemComponent = player.getComponent(CharacterHeldItemComponent.class);
        EntityRef heldItem = characterHeldItemComponent.selectedItem;
        BlockItemComponent blockItemComponent = heldItem.getComponent(BlockItemComponent.class);
        for (String item : lure.luringItems) {
            if (blockItemComponent != null && blockItemComponent.blockFamily.getURI().equals(new BlockUri(item))) {
                return BehaviorState.SUCCESS;
            }
        }
        return BehaviorState.FAILURE;
    }
}

