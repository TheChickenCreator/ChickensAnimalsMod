package chicken.creaturecorner.client.renderer.obj;

import chicken.creaturecorner.client.model.EndoveModel;
import chicken.creaturecorner.server.entity.obj.EndoveEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EndoveRenderer extends GeoEntityRenderer<EndoveEntity> {

   public EndoveRenderer(EntityRendererProvider.Context renderManager) {
       super(renderManager, new EndoveModel());
   }

}
