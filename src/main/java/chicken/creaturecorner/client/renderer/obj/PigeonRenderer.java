package chicken.creaturecorner.client.renderer.obj;

import chicken.creaturecorner.client.model.PigeonModel;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

 public class PigeonRenderer extends GeoEntityRenderer<PigeonEntity> {

    public PigeonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PigeonModel());
    }

}
