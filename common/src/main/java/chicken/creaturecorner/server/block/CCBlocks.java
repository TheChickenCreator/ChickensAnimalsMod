package chicken.creaturecorner.server.block;

import chicken.creaturecorner.platform.Services;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CCBlocks {


    public static Supplier<Block> register(String s, Supplier<Block> block, Item.Properties properties) {
        block = Suppliers.memoize(block);
        Services.PLATFORM.register(BuiltInRegistries.BLOCK, s, block);
        Supplier<Block> finalBlock = block;
        Services.PLATFORM.register(BuiltInRegistries.ITEM, s, () -> new BlockItem(finalBlock.get(), properties));
        return block;
    }

    public static Supplier<Block> registerNoItem(String s, Supplier<Block> block) {
        block = Suppliers.memoize(block);
        Services.PLATFORM.register(BuiltInRegistries.BLOCK, s, block);
        return block;
    }
}
