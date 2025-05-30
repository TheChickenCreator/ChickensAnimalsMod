package chicken.creaturecorner.server.block.sets;

import chicken.creaturecorner.platform.Services;
import chicken.creaturecorner.platform.services.IPlatformHelper;
import chicken.creaturecorner.server.block.CCBlocks;
import com.google.common.base.Supplier;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Getter
public class CCBasicBlockSet {
    private static final IPlatformHelper PLATFORM_HELPER = Services.PLATFORM;

    public final Supplier<Block> BLOCK;
    public final Supplier<Block> SLAB;
    public final Supplier<Block> STAIRS;

    public CCBasicBlockSet(String name, BlockBehaviour.Properties baseBlock, Item.Properties empty) {
        this.BLOCK = CCBlocks.register(name, () -> new Block(baseBlock), empty);
        this.SLAB = CCBlocks.register(name + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
        this.STAIRS = CCBlocks.register(name + "_stair", () -> Services.GENERIC.getNewStairBlock(BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(BLOCK.get())), empty);
    }
}
