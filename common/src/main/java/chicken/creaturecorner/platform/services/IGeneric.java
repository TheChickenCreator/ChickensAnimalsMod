package chicken.creaturecorner.platform.services;

import com.google.common.base.Supplier;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public interface IGeneric {
    StairBlock getNewStairBlock(BlockState baseState, BlockBehaviour.Properties properties);
    PressurePlateBlock getNewPressurePlateBlock(BlockSetType type, BlockBehaviour.Properties properties);
    ButtonBlock getNewButtonBlock(BlockSetType type, int ticksToStayPressed, BlockBehaviour.Properties properties);
    BlockSetType getNewBlockSetType(String name, boolean canOpenByHand, boolean canOpenByWindCharge, boolean canButtonBeActivatedByArrows, BlockSetType.PressurePlateSensitivity pressurePlateSensitivity, SoundType soundType, SoundEvent doorClose, SoundEvent doorOpen, SoundEvent trapdoorClose, SoundEvent trapdoorOpen, SoundEvent pressurePlateClickOff, SoundEvent pressurePlateClickOn, SoundEvent buttonClickOff, SoundEvent buttonClickOn);
    WoodType getNewWoodType(String id, BlockSetType woodSetType);
    Block getNewDoorBlock(BlockSetType woodSetType, BlockBehaviour.Properties properties);
    Block getNewTrapdoorBlock(BlockSetType woodSetType, BlockBehaviour.Properties properties);
    <a extends TrunkPlacer> TrunkPlacerType<a> createTrunkPlacerType(MapCodec<a> codec);
    SaplingBlock createSappling(TreeGrower sequoina, BlockBehaviour.Properties properties);
    <T extends BlockEntity> BlockEntityType<T> createBEType(AccessUtil.BlockEntitySupplier<T> aNew, Block... blocks);
    CreativeModeTab createTab(ItemStack defaultInstance, CreativeModeTab.DisplayItemsGenerator items, MutableComponent translatable);

    Item createSpawnEgg(java.util.function.Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties props);
}
