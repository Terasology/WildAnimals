/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.wildAnimals;

import com.google.common.collect.Lists;
import org.terasology.utilities.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.event.OnChunkGenerated;

import java.util.List;
import java.util.Random;

@RegisterSystem(RegisterMode.AUTHORITY)
public class WildAnimalsSpawnSystem extends BaseComponentSystem {
    private static final int MIN_GROUND_PER_DEER = 5;
    private static final int MIN_DEER_GROUP_SIZE = 4;
    private static final int MAX_DEER_GROUP_SIZE = 10;
    private static final int SPAWN_CHANCE_IN_PERCENT = 10;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    private Random random = new Random();

    private Block grassBlock;
    private Block airBlock;

    private Prefab deerPrefab;
    
    
    /**
     * Readies the spawning system by defining blocks for identification and obtaining prefabs of animals.
     */
    @Override
    public void initialise() {
        grassBlock = blockManager.getBlock("core:Grass");
        airBlock = blockManager.getBlock(BlockManager.AIR_ID);
        deerPrefab = Assets.getPrefab("WildAnimals:Deer").get();
    }
    
    /**
     * Runs upon a chunk being generated. If the SPAWN_CHANCE_IN_PERCENT is above a random value
     * ( < 100), then it'll try to spawn deer on the chunk.
     *
     * @param event         The event which the method will run upon receiving
     * @param worldEntity   The world that the chunk is in
     */
    @ReceiveEvent
    public void onChunkGenerated(OnChunkGenerated event, EntityRef worldEntity) {
        boolean trySpawn = SPAWN_CHANCE_IN_PERCENT > random.nextInt(100);
        if (!trySpawn) {
            return;
        }
        Vector3i chunkPos = event.getChunkPos();
        tryDeerSpawn(chunkPos);
    }
    
    /**
     * Attempts to spawn deer on the specified chunk. The number of deers spawned will depend on four
     * 'private static final int' values defined earlier.
     *
     * @param chunkPos   The chunk which the game will try to spawn deers on
     */
    private void tryDeerSpawn(Vector3i chunkPos) {
        List<Vector3i> foundPositions = findDeerSpawnPositions(chunkPos);

        if (foundPositions.size() < MIN_DEER_GROUP_SIZE * MIN_GROUND_PER_DEER) {
            return;
        }

        int maxDeerCount = foundPositions.size() / MIN_DEER_GROUP_SIZE;
        if (maxDeerCount > MAX_DEER_GROUP_SIZE) {
            maxDeerCount = MAX_DEER_GROUP_SIZE;
        }
        int deerCount = random.nextInt(maxDeerCount - MIN_DEER_GROUP_SIZE) + MIN_DEER_GROUP_SIZE;

        for (int i = 0; i < deerCount; i++ ) {
            int randomIndex = random.nextInt(foundPositions.size());
            Vector3i randomSpawnPosition = foundPositions.remove(randomIndex);
            spawnDeer(randomSpawnPosition);
        }
    }
    
    /**
     * Checks each block of the chunk specified if it's a valid spawning point for deer. If it is, 
     * stores it in a List, which is returned after all blocks are checked.
     *
     * @param chunkPos  The chunk that is being checked for valid spawnpoints
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
                    if (isValidSpawnPosition(blockPos)) {
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
     * @param location   The location where the deer is to be spawned
     */
    private void spawnDeer(Vector3i location) {
        Vector3f floatVectorLocation = location.toVector3f();
        Vector3f yAxis = new Vector3f(0, 1, 0);
        float randomAngle = (float) (random.nextFloat()*Math.PI*2);
        Quat4f rotation = new Quat4f(yAxis, randomAngle);
        entityManager.create(deerPrefab, floatVectorLocation, rotation);
    }

    /**
     * Checks the block below the position, and if the block is either a grass, air, or a penetrable
     * block, then it returns false, signifying that the block is not valid for spawning.
     *
     * @param pos   The block to be checked if it's a valid spot for spawning
     * @return A boolean with the value of true if the block is a valid spot for spawing
     */
    private boolean isValidSpawnPosition(Vector3i pos) {
        Vector3i below = new Vector3i(pos.x, pos.y-1, pos.z);
        Block blockBelow= worldProvider.getBlock(below);
        if (!blockBelow.equals(grassBlock)) {
            return false;
        }
        Block blockAtPosition = worldProvider.getBlock(pos);
        if (!blockAtPosition.isPenetrable()) {
            return false;
        }

        Vector3i above = new Vector3i(pos.x, pos.y+1, pos.z);
        Block blockAbove= worldProvider.getBlock(above);
        if (!blockAbove.equals(airBlock)) {
            return false;
        }
        return true;
    }

}
