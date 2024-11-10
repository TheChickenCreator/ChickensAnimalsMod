package chicken.creaturecorner.client.renderer.obj;

import chicken.creaturecorner.client.model.CoyoteModel;
import chicken.creaturecorner.server.entity.obj.CoyoteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CoyoteRenderer extends GeoEntityRenderer<CoyoteEntity> {

   public CoyoteRenderer(EntityRendererProvider.Context renderManager) {
       super(renderManager, new CoyoteModel());
   }

}
