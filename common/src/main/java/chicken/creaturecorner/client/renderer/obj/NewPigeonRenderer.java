package chicken.creaturecorner.client.renderer.obj;

import chicken.creaturecorner.client.model.NewPigeonModel;
import chicken.creaturecorner.client.model.PigeonModel;
import chicken.creaturecorner.server.entity.obj.NewPigeonEntity;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NewPigeonRenderer extends GeoEntityRenderer<NewPigeonEntity> {

   public NewPigeonRenderer(EntityRendererProvider.Context renderManager) {
       super(renderManager, new NewPigeonModel());
   }

}
