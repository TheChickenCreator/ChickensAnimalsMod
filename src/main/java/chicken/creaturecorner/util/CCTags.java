package chicken.creaturecorner.util;

import chicken.creaturecorner.AnimalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class CCTags {

    public static class Biomes {

        public static final TagKey<Biome> COYOTE_BIOMES = create("is_coyote_biome");

        public static final TagKey<Biome> CARACARA_BIOMES = create("is_caracara_biome");

        public static final TagKey<Biome> PIGEON_BIOMES = create("is_pigeon_biome");

        public static final TagKey<Biome> ENDOVE_BIOMES = create("is_endove_biome");

        private static TagKey<Biome> create(String pName) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, pName));
        }
    }

}
