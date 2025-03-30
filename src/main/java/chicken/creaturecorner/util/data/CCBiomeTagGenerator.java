package chicken.creaturecorner.util.data;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.util.CCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class CCBiomeTagGenerator extends BiomeTagsProvider {

    public CCBiomeTagGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, AnimalMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.addTags();
    }

    protected void addTags() {
        tag(CCTags.Biomes.CARACARA_BIOMES).addTags(BiomeTags.SPAWNS_WARM_VARIANT_FROGS);
        tag(CCTags.Biomes.COYOTE_BIOMES).add(Biomes.BIRCH_FOREST).add(Biomes.BADLANDS).add(Biomes.SNOWY_PLAINS);

    }
}
