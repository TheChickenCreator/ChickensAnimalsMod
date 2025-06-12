package chicken.creaturecorner.client;

import chicken.creaturecorner.CCConstants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class CCModelLayers {

    public static final ModelLayerLocation GALLIAN =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "gallian"), "main");

    public static final ModelLayerLocation GALLIAN_CHICK =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "gallian_chick"), "main");
}
