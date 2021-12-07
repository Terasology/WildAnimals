// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.system;

import com.google.common.collect.Lists;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.event.OnChunkGenerated;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.wildAnimals.AnimalSpawnConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Share(value = WildAnimalsSpawnSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class WildAnimalsSpawnSystem extends BaseComponentSystem {
    private AnimalSpawnConfig config;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    private final Random random = new Random();

    private Block grassBlock;
    private Block airBlock;
    private List<Prefab> flockAnimals;

    /**
     * Check blocks at and around the target position and check if it's a valid spawning spot
     */
    private Function<Vector3i, Boolean> isValidSpawnPosition;


    /**
     * Readies the spawning system by defining blocks for identification and obtaining prefabs of animals.
     */
    @Override
    public void initialise() {
        grassBlock = blockManager.getBlock("CoreAssets:Grass");
        airBlock = blockManager.getBlock(BlockManager.AIR_ID);
        flockAnimals = new ArrayList<>();
        Assets.getPrefab("WildAnimals:Deer").ifPresent(prefab -> flockAnimals.add(prefab));
        Assets.getPrefab("WildAnimals:Sheep").ifPresent(prefab -> flockAnimals.add(prefab));

        if (isValidSpawnPosition == null) {
            isValidSpawnPosition = pos -> {
                Vector3i below = new Vector3i(pos.x, pos.y - 1, pos.z);
                Block blockBelow = worldProvider.getBlock(below);
                if (!blockBelow.equals(grassBlock)) {
                    return false;
                }
                Block blockAtPosition = worldProvider.getBlock(pos);
                if (!blockAtPosition.isPenetrable()) {
                    return false;
                }

                Vector3i above = new Vector3i(pos.x, pos.y + 1, pos.z);
                Block blockAbove = worldProvider.getBlock(above);
                return blockAbove.equals(airBlock);
            };
        }

        if (config == null) {
            config = new AnimalSpawnConfig();
        }
    }

    public void setSpawnCondition(Function<Vector3i, Boolean> function) {
        isValidSpawnPosition = function;
    }

    public void setConfig(AnimalSpawnConfig configuration) {
        config = configuration;
    }


    /**
     * Runs upon a chunk being generated to see whether a deer should be spawned
     *
     * @param event The event which the method will run upon receiving
     * @param worldEntity The world that the chunk is in
     */
    @ReceiveEvent
    public void onChunkGenerated(OnChunkGenerated event, EntityRef worldEntity) {
        boolean trySpawn = config.spawnChanceInPercent > random.nextInt(100);
        if (!trySpawn) {
            return;
        }
        Vector3ic chunkPos = event.getChunkPos();
        // randomly decide whether to spawn deer or sheep in this chunk
        Prefab animalPrefab = flockAnimals.get(random.nextInt(flockAnimals.size()));
        tryFlockAnimalSpawn(animalPrefab, chunkPos);
    }

    /**
     * Attempts to spawn deer on the specified chunk. The number of deers spawned will depend on probabiliy
     * configurations defined earlier.
     *
     * @param chunkPos The chunk which the game will try to spawn deers on
     */
    private void tryFlockAnimalSpawn(Prefab animalPrefab, Vector3ic chunkPos) {
        List<Vector3i> foundPositions = findFlockAnimalSpawnPositions(chunkPos);

        if (foundPositions.size() < config.minFlockSize * config.minGroundPerFlockAnimal) {
            return;
        }

        int maxDeerCount = foundPositions.size() / config.minFlockSize;
        if (maxDeerCount > config.maxFlockSize) {
            maxDeerCount = config.maxFlockSize;
        }
        int deerCount = random.nextInt(maxDeerCount - config.minFlockSize) + config.minFlockSize;

        for (int i = 0; i < deerCount; i++) {
            int randomIndex = random.nextInt(foundPositions.size());
            Vector3i randomSpawnPosition = foundPositions.remove(randomIndex);
            spawnFlockAnimal(animalPrefab, randomSpawnPosition);
        }
    }

    /**
     * Checks each block of the chunk specified for valid spawning spawnings point for deer.
     *
     * @param chunkPos The chunk that is being checked for valid spawnpoints
     * @return a list of positions of potential deer spawnpoints
     */
    private List<Vector3i> findFlockAnimalSpawnPositions(Vector3ic chunkPos) {
        Vector3i worldPos = new Vector3i(chunkPos);
        worldPos.mul(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z);
        List<Vector3i> foundPositions = Lists.newArrayList();
        Vector3i blockPos = new Vector3i();
        for (int y = Chunks.SIZE_Y - 1; y >= 0; y--) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                for (int x = 0; x < Chunks.SIZE_X; x++) {
                    blockPos.set(x + worldPos.x, y + worldPos.y, z + worldPos.z);
                    if (isValidSpawnPosition.apply(blockPos)) {
                        foundPositions.add(new Vector3i(blockPos));
                    }
                }
            }
        }
        return foundPositions;
    }

    /**
     * Spawns the deer at the location specified by the parameter.
     *
     * @param location The location where the deer is to be spawned
     */
    private void spawnFlockAnimal(Prefab animalPrefab, Vector3i location) {
        Vector3f floatVectorLocation = new Vector3f(location);
        Vector3f yAxis = new Vector3f(0, 1, 0);
        float randomAngle = (float) (random.nextFloat() * Math.PI * 2);
        Quaternionf rotation = new Quaternionf(new AxisAngle4f(randomAngle, yAxis));
        // TODO Turn deer spawning back on when done with debugging - this and the SPAWN_CHANCE_IN_PERCENT constant.
        entityManager.create(animalPrefab, floatVectorLocation, rotation);
    }

}
