package chicken.creaturecorner;

import chicken.creaturecorner.server.block.CCBlocks;
import chicken.creaturecorner.server.blockentity.CCBlockEntities;
import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.item.CCItemGroups;
import chicken.creaturecorner.server.item.CCItems;
import chicken.creaturecorner.server.sound.CCSounds;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCCommon {
    public static final Logger LOGGER = LoggerFactory.getLogger(CCConstants.MOD_ID);

    public static void init() {
        new CCEntities();
        new CCBlocks();
        new CCItems();
        new CCBlockEntities();
        new CCItemGroups();
        new CCSounds();
    }

    public static ResourceLocation createId(String s) {
        return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, s);
    }
}
