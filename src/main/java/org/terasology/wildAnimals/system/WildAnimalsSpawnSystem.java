// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimals.system;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
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
import org.terasology.engine.world.chunks.ChunkConstants;
import org.terasology.engine.world.chunks.event.OnChunkGenerated;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.wildAnimals.AnimalSpawnConfig;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Share(value = WildAnimalsSpawnSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class WildAnimalsSpawnSystem extends BaseComponentSystem {
    private final Random random = new Random();
    private AnimalSpawnConfig config;
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    private Block grassBlock;
    private Block airBlock;

    private Prefab deerPrefab;
    private Prefab redDeerPrefab;
    private Prefab greenDeerPrefab;
    private Prefab blueDeerPrefab;

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
        deerPrefab = Assets.getPrefab("WildAnimals:Deer").get();
        redDeerPrefab = Assets.getPrefab("WildAnimals:RedDeer").get();
        greenDeerPrefab = Assets.getPrefab("WildAnimals:GreenDeer").get();
        blueDeerPrefab = Assets.getPrefab("WildAnimals:BlueDeer").get();

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
        boolean trySpawn = config.SPAWN_CHANCE_IN_PERCENT > random.nextInt(100);
        if (!trySpawn) {
            return;
        }
        Vector3i chunkPos = event.getChunkPos();
        tryDeerSpawn(chunkPos);
    }

    /**
     * Attempts to spawn deer on the specified chunk. The number of deers spawned will depend on probabiliy
     * configurations defined earlier.
     *
     * @param chunkPos The chunk which the game will try to spawn deers on
     */
    private void tryDeerSpawn(Vector3i chunkPos) {
        List<Vector3i> foundPositions = findDeerSpawnPositions(chunkPos);

        if (foundPositions.size() < config.MIN_DEER_GROUP_SIZE * config.MIN_GROUND_PER_DEER) {
            return;
        }

        int maxDeerCount = foundPositions.size() / config.MIN_DEER_GROUP_SIZE;
        if (maxDeerCount > config.MAX_DEER_GROUP_SIZE) {
            maxDeerCount = config.MAX_DEER_GROUP_SIZE;
        }
        int deerCount = random.nextInt(maxDeerCount - config.MIN_DEER_GROUP_SIZE) + config.MIN_DEER_GROUP_SIZE;

        for (int i = 0; i < deerCount; i++) {
            int randomIndex = random.nextInt(foundPositions.size());
            Vector3i randomSpawnPosition = foundPositions.remove(randomIndex);
            spawnDeer(randomSpawnPosition);
        }
    }

    /**
     * Checks each block of the chunk specified for valid spawning spawnings point for deer.
     *
     * @param chunkPos The chunk that is being checked for valid spawnpoints
     * @return a list of positions of potential deer spawnpoints
     */
    private List<Vector3i> findDeerSpawnPositions(Vector3i chunkPos) {
        Vector3i worldPos = new Vector3i(chunkPos);
        worldPos.mul(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
        List<Vector3i> foundPositions = Lists.newArrayList();
        Vector3i blockPos = new Vector3i();
        for (int y = ChunkConstants.SIZE_Y - 1; y >= 0; y--) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
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
    private void spawnDeer(Vector3i location) {
        Vector3f floatVectorLocation = location.toVector3f();
        Vector3f yAxis = new Vector3f(0, 1, 0);
        float randomAngle = (float) (random.nextFloat() * Math.PI * 2);
        Quat4f rotation = new Quat4f(yAxis, randomAngle);
        // TODO Turn deer spawning back on when done with debugging - this and the SPAWN_CHANCE_IN_PERCENT constant.
        entityManager.create(deerPrefab, floatVectorLocation, rotation);
    }

}
