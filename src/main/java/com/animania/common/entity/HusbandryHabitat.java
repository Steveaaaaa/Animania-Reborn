package com.animania.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.*;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;

/** A bounded walkable-area sample. Unloaded chunks are never queried for blocks. */
final class HusbandryHabitat {
    int area, barriers;
    boolean land, incomplete, open, shelter, grass, rooting, mud, dust, browse, climb, perch, scratch, wheel, water, flowers, hive;
    final Set<BlockPos> cells = new HashSet<>();
    static HusbandryHabitat inspect(Animal animal) {
        HusbandryHabitat h = new HusbandryHabitat();
        BlockPos origin = animal.blockPosition();
        if (animal instanceof com.animania.modern.ModernBee bee) {
            BlockPos home = bee.husbandryHive();
            if (home != null && !animal.level().hasChunkAt(home)) { h.incomplete = true; return h; }
            if (home != null && animal.level().hasChunkAt(home)) {
                origin = home;
                h.hive = animal.level().getBlockState(home).is(com.animania.common.registry.ModBlocks.HIVE.get())
                        || animal.level().getBlockState(home).is(BlockTags.BEEHIVES);
                var hiveState = animal.level().getBlockState(home);
                if (h.hive && hiveState.hasProperty(com.animania.farm.world.block.HiveBlock.FACING)) {
                    BlockPos entrance = home.relative(hiveState.getValue(com.animania.farm.world.block.HiveBlock.FACING));
                    if (!animal.level().hasChunkAt(entrance)) h.incomplete = true;
                    else h.hive = animal.level().getBlockState(entrance).getCollisionShape(animal.level(), entrance).isEmpty()
                            && animal.level().getFluidState(entrance).isEmpty();
                }
            }
            h.area = 100;
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-6,-3,-6), origin.offset(6,3,6))) {
                if (!animal.level().hasChunkAt(p)) { h.incomplete = true; continue; }
                var state = animal.level().getBlockState(p);
                h.flowers |= state.is(BlockTags.FLOWERS);
                h.water |= state.getFluidState().is(FluidTags.WATER);
            }
            return h;
        }
        boolean aquatic = animal instanceof com.animania.modern.ModernAxolotl;
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(origin); h.cells.add(origin);
        while (!pending.isEmpty() && h.cells.size() < 192) {
            BlockPos p = pending.removeFirst();
            h.observe(animal, p);
            for (Direction direction : aquatic ? Direction.values() : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}) {
                BlockPos next = p.relative(direction);
                if (Math.abs(next.getX()-origin.getX()) > 8 || Math.abs(next.getZ()-origin.getZ()) > 8
                        || Math.abs(next.getY()-origin.getY()) > 3) { h.open = true; continue; }
                if (!animal.level().hasChunkAt(next)) { h.incomplete = true; h.open = true; continue; }
                if (!walkable(animal, next, aquatic) && !aquatic) {
                    if (walkable(animal, next.above(), false)) next = next.above();
                    else if (walkable(animal, next.below(), false)) next = next.below();
                }
                if (Math.abs(next.getY()-origin.getY()) > 3 || !walkable(animal, next, aquatic)) continue;
                if (h.cells.add(next.immutable())) pending.add(next.immutable());
            }
        }
        if (!pending.isEmpty()) h.open = true;
        h.area = h.cells.size();
        return h;
    }
    private static boolean walkable(Animal animal, BlockPos pos, boolean aquatic) {
        var level = animal.level();
        if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.above())) return false;
        if (aquatic || animal instanceof com.animania.extra.amphibian.AnimaniaAmphibian && level.getFluidState(pos).is(FluidTags.WATER)) return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getBlockState(pos).getCollisionShape(level,pos).isEmpty();
        if (!level.getFluidState(pos).isEmpty() || !level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP)) return false;
        return level.getBlockState(pos).getCollisionShape(level,pos).isEmpty()
                && (animal.getBbHeight() < 1 || level.getBlockState(pos.above()).getCollisionShape(level,pos.above()).isEmpty());
    }
    private void observe(Animal animal, BlockPos p) {
        var level = animal.level();
        var floor = level.getBlockState(p.below());
        grass |= floor.is(Blocks.GRASS_BLOCK) || floor.is(Blocks.HAY_BLOCK);
        rooting |= floor.is(BlockTags.DIRT) || floor.is(BlockTags.SAND) || floor.is(Blocks.HAY_BLOCK);
        dust |= floor.is(BlockTags.SAND) || floor.is(Blocks.DIRT) || floor.is(Blocks.COARSE_DIRT);
        mud |= floor.is(Blocks.MUD) || floor.is(com.animania.common.registry.ModBlocks.MUD.get());
        climb |= p.getY() != animal.blockPosition().getY() || floor.is(BlockTags.LOGS);
        land |= level.getFluidState(p).isEmpty() && floor.isFaceSturdy(level,p.below(),Direction.UP);
        water |= level.getFluidState(p).is(FluidTags.WATER);
        for (int dy = 1; dy <= 4; dy++) {
            var roof = level.getBlockState(p.above(dy));
            if (!roof.getCollisionShape(level,p.above(dy)).isEmpty()) { shelter = true; break; }
        }
        for (Direction direction : Direction.values()) {
            BlockPos q = p.relative(direction);
            if (!level.hasChunkAt(q)) continue;
            var state = level.getBlockState(q);
            String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            if (state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.getBlock() instanceof FenceGateBlock
                    || direction.getAxis().isHorizontal() && (state.is(BlockTags.PLANKS) || id.contains("glass") || state.is(Blocks.BRICKS) || state.is(Blocks.STONE_BRICKS))) barriers++;
            browse |= state.is(BlockTags.LEAVES);
            flowers |= state.is(BlockTags.FLOWERS);
            water |= state.getFluidState().is(FluidTags.WATER);
            scratch |= state.is(BlockTags.LOGS) || id.equals("cat_tower");
            climb |= id.equals("cat_tower");
            wheel |= id.equals("block_hamster_wheel");
            shelter |= id.startsWith("cat_bed") || id.equals("dog_house") || id.equals("dog_pillow");
            perch |= (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) && level.isEmptyBlock(q.above());
        }
    }
    boolean enclosed() { return !open && barriers >= 2 && area > 1; }
}
