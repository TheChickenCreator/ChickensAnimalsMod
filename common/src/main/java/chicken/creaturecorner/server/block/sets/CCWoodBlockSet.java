package chicken.creaturecorner.server.block.sets;

import chicken.creaturecorner.platform.Services;
import chicken.creaturecorner.server.block.CCBlocks;
import chicken.creaturecorner.server.block.obj.vanilla_overrides.CCCeilingHangingSignBlock;
import chicken.creaturecorner.server.block.obj.vanilla_overrides.CCStandingSignBlock;
import chicken.creaturecorner.server.block.obj.vanilla_overrides.CCWallHangingSignBlock;
import chicken.creaturecorner.server.block.obj.vanilla_overrides.CCWallSignBlock;
import chicken.creaturecorner.server.item.CCItems;
import com.google.common.base.Supplier;
import lombok.Getter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

@Getter
public class CCWoodBlockSet {
    public final Supplier<Block> BLOCK;
    public final Supplier<Block> SLAB;
    public final Supplier<Block> STAIRS;
    public final Supplier<Block> FENCE;
    public final Supplier<Block> FENCE_GATE;
    public final Supplier<Block> LOG;
    public final Supplier<Block> WOOD;
    public final Supplier<Block> STRIPPED_WOOD;
    public final Supplier<Block> STRIPPED_LOG;
    public final Supplier<Block> DOOR;
    public final Supplier<Block> BUTTON;
    public final Supplier<Block> PRESSURE_PLATE;
    public final Supplier<Block> TRAPDOOR;
    public final Supplier<Block> SIGN;
    public final Supplier<Item> SIGN_ITEM;
    public final Supplier<Block> WALL_SIGN;
    public final Supplier<Block> HANGING_SIGN;
    public final Supplier<Block> HANGING_SIGN_WALL;
    public final Supplier<Item> HANGING_SIGN_ITEM;
    public final Supplier<Block> LEAVES;

    public final WoodType woodType;
    public final BlockSetType woodSetType;

    public CCWoodBlockSet(String name, MapColor color, Item.Properties empty) {
        woodSetType = Services.GENERIC.getNewBlockSetType(
                name,
                true,
                true,
                true,
                BlockSetType.PressurePlateSensitivity.EVERYTHING,
                SoundType.WOOD,
                SoundEvents.WOODEN_DOOR_CLOSE,
                SoundEvents.WOODEN_DOOR_OPEN,
                SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF,
                SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                SoundEvents.WOODEN_BUTTON_CLICK_ON
        );
        woodType = Services.GENERIC.getNewWoodType("newworld:" + name, woodSetType);
        BLOCK = CCBlocks.register(name, () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(color)), empty);
        SLAB = CCBlocks.register(name + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).mapColor(color)), empty);
        STAIRS = CCBlocks.register(name + "_stairs", () -> Services.GENERIC.getNewStairBlock(BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS).mapColor(color)), empty);
        FENCE = CCBlocks.register(name + "_fence", () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).mapColor(color)), empty);
        FENCE_GATE = CCBlocks.register(name + "_fence_gate", () -> new FenceGateBlock(woodType, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE).mapColor(color)), empty);
        STRIPPED_LOG = CCBlocks.register("stripped_" + name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).mapColor(color)), empty);
        LOG = CCBlocks.register(name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).mapColor(color)), empty);
        WOOD = CCBlocks.register(name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).mapColor(color)), empty);
        STRIPPED_WOOD = CCBlocks.register("stripped_" + name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD).mapColor(color)), empty);
        DOOR = CCBlocks.register(name + "_door", () -> Services.GENERIC.getNewDoorBlock(woodSetType,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR).mapColor(color)), empty);
        BUTTON = CCBlocks.register(name + "_button", () -> Services.GENERIC.getNewButtonBlock(woodSetType, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON).mapColor(color)), empty);
        PRESSURE_PLATE = CCBlocks.register(name + "_pressure_plate", () -> Services.GENERIC.getNewPressurePlateBlock(woodSetType, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE).mapColor(color)), empty);
        SIGN = CCBlocks.registerNoItem(name + "_sign", () -> new CCStandingSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).mapColor(color), woodType));
        TRAPDOOR = CCBlocks.register(name + "_trapdoor", () -> Services.GENERIC.getNewTrapdoorBlock(woodSetType, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).mapColor(color)), empty);
        WALL_SIGN = CCBlocks.registerNoItem(name + "_wall_sign", () -> new CCWallSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN).mapColor(color), woodType));
        HANGING_SIGN = CCBlocks.registerNoItem(name + "_hanging_sign", () -> new CCCeilingHangingSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN).mapColor(color), woodType));
        HANGING_SIGN_WALL = CCBlocks.registerNoItem(name + "_hanging_wall_sign", () -> new CCWallHangingSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN).mapColor(color), woodType));
        LEAVES = CCBlocks.register(name + "_leaves", CCWoodBlockSet::leaves, empty);
        SIGN_ITEM = CCItems.register(name + "_sign_item", () -> new SignItem(empty, SIGN.get(), WALL_SIGN.get()));
        HANGING_SIGN_ITEM = CCItems.register(name + "_hanging_sign_item", () -> new HangingSignItem(HANGING_SIGN.get(), HANGING_SIGN_WALL.get(), empty));
    }

    public static Block leaves() {
        return new LeavesBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .strength(0.2F)
                        .randomTicks()
                        .sound(SoundType.GRASS)
                        .noOcclusion()
                        .isValidSpawn((pState, pLevel, pPos, pValue) -> pValue == EntityType.OCELOT || pValue == EntityType.PARROT)
                        .isSuffocating(((pState, pLevel, pPos) -> false))
                        .isViewBlocking(((pState, pLevel, pPos) -> false))
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY)
                        .isRedstoneConductor(((pState, pLevel, pPos) -> false))
        );
    }
}
