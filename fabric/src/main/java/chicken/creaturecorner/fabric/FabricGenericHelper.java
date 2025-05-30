package chicken.creaturecorner.fabric;

import chicken.creaturecorner.platform.services.AccessUtil;
import chicken.creaturecorner.platform.services.IGeneric;
import com.google.common.base.Supplier;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
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

public class FabricGenericHelper implements IGeneric {
    @Override
    public StairBlock getNewStairBlock(BlockState baseState, BlockBehaviour.Properties properties) {
        return new StairBlock(baseState, properties);
    }

    @Override
    public PressurePlateBlock getNewPressurePlateBlock(BlockSetType type, BlockBehaviour.Properties properties) {
        return new PressurePlateBlock(type, properties);
    }

    @Override
    public ButtonBlock getNewButtonBlock(BlockSetType type, int ticksToStayPressed, BlockBehaviour.Properties properties) {
        return new ButtonBlock(type, ticksToStayPressed, properties);
    }

    @Override
    public BlockSetType getNewBlockSetType(String name, boolean canOpenByHand, boolean canOpenByWindCharge, boolean canButtonBeActivatedByArrows, BlockSetType.PressurePlateSensitivity pressurePlateSensitivity, SoundType soundType, SoundEvent doorClose, SoundEvent doorOpen, SoundEvent trapdoorClose, SoundEvent trapdoorOpen, SoundEvent pressurePlateClickOff, SoundEvent pressurePlateClickOn, SoundEvent buttonClickOff, SoundEvent buttonClickOn) {
        return new BlockSetType(name, canOpenByHand, canOpenByWindCharge, canButtonBeActivatedByArrows, pressurePlateSensitivity, soundType, doorClose, doorOpen, trapdoorClose, trapdoorOpen, pressurePlateClickOff, pressurePlateClickOn, buttonClickOff, buttonClickOn);
    }

    @Override
    public WoodType getNewWoodType(String id, BlockSetType woodSetType) {
        return WoodType.register(new WoodType(id, woodSetType));
    }

    @Override
    public Block getNewDoorBlock(BlockSetType woodSetType, BlockBehaviour.Properties properties) {
        return new DoorBlock(woodSetType, properties);
    }

    @Override
    public Block getNewTrapdoorBlock(BlockSetType woodSetType, BlockBehaviour.Properties properties) {
        return new TrapDoorBlock(woodSetType, properties);
    }

    @Override
    public <a extends TrunkPlacer> TrunkPlacerType<a> createTrunkPlacerType(MapCodec<a> codec) {
        return new TrunkPlacerType<>(codec);
    }

    @Override
    public SaplingBlock createSappling(TreeGrower sequoina, BlockBehaviour.Properties properties) {
        return new SaplingBlock(sequoina, properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBEType(AccessUtil.BlockEntitySupplier<T> aNew, Block... blocks) {
        return BlockEntityType.Builder.of(aNew::create, blocks).build(null);
    }

    @Override
    public CreativeModeTab createTab(ItemStack defaultInstance, CreativeModeTab.DisplayItemsGenerator items, MutableComponent translatable) {
        return FabricItemGroup.builder().title(translatable).displayItems(items).icon(() -> defaultInstance).build();
    }

    @Override
    public Item createSpawnEgg(java.util.function.Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties props) {
        return new SpawnEggItem(type.get(), backgroundColor, highlightColor, props);
    }
}
