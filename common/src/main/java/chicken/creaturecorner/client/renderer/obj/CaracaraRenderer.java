package chicken.creaturecorner.client.renderer.obj;

import chicken.creaturecorner.client.model.CaracaraModel;
import chicken.creaturecorner.server.entity.obj.CaracaraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CaracaraRenderer extends GeoEntityRenderer<CaracaraEntity> {

   public CaracaraRenderer(EntityRendererProvider.Context renderManager) {
       super(renderManager, new CaracaraModel());
   }

}
