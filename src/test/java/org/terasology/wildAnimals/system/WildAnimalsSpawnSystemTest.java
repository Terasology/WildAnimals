// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.wildAnimals.system;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.MainLoop;
import org.terasology.engine.integrationenvironment.jupiter.Dependencies;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.rendering.logic.SkeletalMeshComponent;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.wildAnimals.AnimalSpawnConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies("WildAnimals")
@IntegrationEnvironment(subsystem = WildAnimalsSpawnSystemTest.WriteSaveGames.class)
class WildAnimalsSpawnSystemTest {

    private static final Logger logger = LoggerFactory.getLogger(WildAnimalsSpawnSystemTest.class);

    @BeforeEach
    void configSpawn(WildAnimalsSpawnSystem spawnSystem, WorldProvider worldProvider) {
        var config = new AnimalSpawnConfig();
        config.spawnChanceInPercent = 100;
        config.minFlockSize = 1;
        config.minGroundPerFlockAnimal = 1;
        spawnSystem.setConfig(config);

        // Spawn on any solid ground.
        spawnSystem.setSpawnCondition(pos -> {
            Vector3i below = new Vector3i(pos.x, pos.y - 1, pos.z);
            return worldProvider.getBlock(pos).isPenetrable()
                    && !worldProvider.getBlock(below).isPenetrable();
        });
    }

    @Test
    @Order(1)
    void testCanSave(EntityManager entities, StorageManager storage, LocalPlayer player, MainLoop main) {
        assertThat(GameProvider.isSavesFolderEmpty()).isTrue();

        loadRegionAroundPlayer(player, main);

        assertThat(entities.getEntitiesWith(SkeletalMeshComponent.class)).isNotEmpty();

        var animals = Lists.newArrayList(entities.getEntitiesWith(SkeletalMeshComponent.class));
        assertEveryoneHasBones(animals);

        storage.waitForCompletionOfPreviousSaveAndStartSaving();
        storage.finishSavingAndShutdown();
    }

    @Test
    @Order(2)
    void testCanLoad(EntityManager entities, LocalPlayer player, MainLoop main) {
        assertThat(GameProvider.getSavedGames()).hasSize(1);

        loadRegionAroundPlayer(player, main);

        var animals = Lists.newArrayList(entities.getEntitiesWith(SkeletalMeshComponent.class));
        assertEveryoneHasBones(animals);
    }

    private void loadRegionAroundPlayer(LocalPlayer player, MainLoop main) {
        var character = player.getCharacterEntity();
        var loc = character.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        var relevantRegion = new BlockRegion((int) loc.x, (int) loc.y, (int) loc.z)
                .expand(100, 0, 100);
        main.runUntil(main.makeBlocksRelevant(relevantRegion));
    }

    private void assertEveryoneHasBones(List<EntityRef> animals) {
        assertThat(animals).isNotNull();
        assertThat(animals).isNotEmpty();
        for (EntityRef animal : animals) {
            var skeleton = animal.getComponent(SkeletalMeshComponent.class);
            var a = assertWithMessage("Entity %s animal %s", animal, skeleton);
            a.that(skeleton.boneEntities).isNotNull();
            a.that(skeleton.boneEntities).isNotEmpty();
            skeleton.boneEntities.forEach((name, boneEnt) ->
                    assertWithMessage("Bone \"%s\"", name).that(boneEnt).isNotEqualTo(EntityRef.NULL));
        }
    }

    /** @see <a href="https://github.com/MovingBlocks/Terasology/issues/5050">Terasology#5050</a> */
    static class WriteSaveGames implements EngineSubsystem {

        private static Path homePath;

        @Override
        public String getName() {
            return getClass().getCanonicalName();
        }

        @Override
        public void preInitialise(Context rootContext) {
            if (homePath != null) {
                try {
                    logger.debug("Resetting home path to previously seen {}", homePath);
                    PathManager.getInstance().useOverrideHomePath(homePath);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        @Override
        public void initialise(GameEngine engine, Context rootContext) {
            if (homePath == null) {
                homePath = PathManager.getInstance().getHomePath();
                logger.debug("Home path first set to {}", homePath);
            }
            rootContext.getValue(SystemConfig.class).writeSaveGamesEnabled.set(true);
        }
    }
}
