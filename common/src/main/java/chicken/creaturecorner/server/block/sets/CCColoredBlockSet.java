package chicken.creaturecorner.server.block.sets;

import chicken.creaturecorner.server.block.CCBlocks;
import com.google.common.base.Supplier;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CCColoredBlockSet {
    public final Supplier<Block> BLACK;
    public final Supplier<Block> BLUE;
    public final Supplier<Block> BROWN;
    public final Supplier<Block> CYAN;
    public final Supplier<Block> GREEN;
    public final Supplier<Block> GREY;
    public final Supplier<Block> LIGHTBLUE;
    public final Supplier<Block> LIGHTGREY;
    public final Supplier<Block> LIME;
    public final Supplier<Block> MAGENTA;
    public final Supplier<Block> ORANGE;
    public final Supplier<Block> PINK;
    public final Supplier<Block> PURPLE;
    public final Supplier<Block> RED;
    public final Supplier<Block> WHITE;
    public final Supplier<Block> YELLOW;

    @Getter
    private final String name;
    private final Supplier<Block> blockSupplier;
    private final Item.Properties properties;
    public CCColoredBlockSet(String baseName, Supplier<Block> blockSupplier, Item.Properties properties) {
        this.name = baseName;
        this.blockSupplier = blockSupplier;
        this.properties = properties;
        BLACK = createRegistry("_black");
        BLUE = createRegistry("_blue");
        BROWN = createRegistry("_brown");
        CYAN = createRegistry("_cyan");
        GREEN = createRegistry("_green");
        GREY = createRegistry("_gray");
        LIGHTBLUE = createRegistry("_light_blue");
        LIGHTGREY = createRegistry("_light_gray");
        LIME = createRegistry("_lime");
        MAGENTA = createRegistry("_magenta");
        ORANGE = createRegistry("_orange");
        PINK = createRegistry("_pink");
        PURPLE = createRegistry("_purple");
        RED = createRegistry("_red");
        WHITE = createRegistry("_white");
        YELLOW = createRegistry("_yellow");
    }

    private Supplier<Block> createRegistry(String color) {
        return CCBlocks.register(name + color, blockSupplier, properties);
    }
}
