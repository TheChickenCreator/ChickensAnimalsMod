package chicken.creaturecorner.server.block.sets;

import chicken.creaturecorner.platform.Services;
import chicken.creaturecorner.server.block.CCBlocks;
import com.google.common.base.Supplier;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

@Getter
public class CCStoneBlockSet {
    public final Supplier<Block> WALL;
    public final Supplier<Block> SLAB;
    public final Supplier<Block> STAIRS;
    public final Supplier<Block> BLOCK;
    public final Supplier<Block> BUTTON;
    public final Supplier<Block> PRESSURE_PLATE;
    public final Supplier<Block> INFESTED_BLOCK;

    public CCStoneBlockSet(String name, BlockBehaviour.Properties baseBlock, Item.Properties empty) {
        BLOCK = CCBlocks.register(name, () -> new Block(baseBlock), empty);
        BUTTON = CCBlocks.register(name + "_button", () -> Services.GENERIC.getNewButtonBlock(BlockSetType.STONE, 20, BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
        PRESSURE_PLATE = CCBlocks.register(name + "_pressure_plate", () -> Services.GENERIC.getNewPressurePlateBlock(BlockSetType.STONE, BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
        INFESTED_BLOCK = CCBlocks.register("infested_" + name, () -> new InfestedBlock(BLOCK.get(), BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
        SLAB = CCBlocks.register(name + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
        STAIRS = CCBlocks.register(name + "_stair", () -> Services.GENERIC.getNewStairBlock(BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
        WALL = CCBlocks.register(name +"_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
    }
}
