package chicken.creaturecorner.server.entity;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.platform.Services;
import chicken.creaturecorner.server.entity.obj.*;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class CCEntities {

    public static final Supplier<EntityType<PigeonEntity>> PIGEON_TYPE =
            register("pigeon",
                    () -> EntityType.Builder.of(PigeonEntity::new, MobCategory.CREATURE)
                            .sized(0.7f, 0.9f)
                            .build(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "pigeon").toString()));

//    public static final Supplier<EntityType<NewPigeonEntity>> NEW_PIGEON =
//            register("new_pigeon",
//                    () -> EntityType.Builder.of(NewPigeonEntity::new, MobCategory.CREATURE)
//                            .sized(0.7f, 0.9f)
//                            .build(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "new_pigeon").toString()));

    public static final Supplier<EntityType<EndoveEntity>> ENDOVE_TYPE =
            register("endove",
                    () -> EntityType.Builder.of(EndoveEntity::new, MobCategory.CREATURE)
                            .sized(0.7f, 0.9f)
                            .build(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "endove").toString()));

    public static final Supplier<EntityType<CaracaraEntity>> CARACARA_TYPE =
            register("caracara",
                    () -> EntityType.Builder.of(CaracaraEntity::new, MobCategory.CREATURE)
                            .sized(1f, 1f)
                            .build(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "caracara").toString()));

    public static final Supplier<EntityType<CoyoteEntity>> COYOTE_TYPE =
            register("coyote",
                    () -> EntityType.Builder.of(CoyoteEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.8F)
                            .build(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "coyote").toString()));


    public static final Supplier<EntityType<GallianEntity>> GALLIAN =
            register("gallian",
                    () -> EntityType.Builder.of(GallianEntity::new, MobCategory.CREATURE)
                            .sized(1.25f, 2.9f)
                            .build(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "gallian").toString()));


    public static<T extends Entity> Supplier<EntityType<T>> register(String s, Supplier<EntityType<T>> item) {
        item = Suppliers.memoize(item);
        Services.PLATFORM.register(BuiltInRegistries.ENTITY_TYPE, s, item::get);
        return item;
    }
}
