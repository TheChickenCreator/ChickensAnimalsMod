package chicken.creaturecorner.server.block.sets;

import chicken.creaturecorner.platform.Services;
import com.google.common.base.Supplier;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Getter
public class CCColoredBlockSets {
    private final String name;
    private final Supplier<Block> blockSupplier;
    private final Item.Properties properties;

    private final CCColoredBlockSet base;
    private final CCColoredBlockSet stair;
    private final CCColoredBlockSet slab;
    private final CCColoredBlockSet wall;

    public CCColoredBlockSets(String baseName, Supplier<Block> blockSupplier, Item.Properties properties, boolean hasStair, boolean hasSlab, boolean hasWall) {
        this.name = baseName;
        this.blockSupplier = blockSupplier;
        this.properties = properties;
        base = new CCColoredBlockSet(baseName, blockSupplier, properties);
        stair = hasStair ? new CCColoredBlockSet(baseName + "_stairs", () -> Services.GENERIC.getNewStairBlock(base.BLACK.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(base.BLACK.get())), properties) : null;
        slab = hasSlab ? new CCColoredBlockSet(baseName + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(base.BLACK.get())), properties) : null;
        wall = hasWall ? new CCColoredBlockSet(baseName + "_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(base.BLACK.get())), properties) : null;
    }
}
