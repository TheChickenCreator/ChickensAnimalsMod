package chicken.creaturecorner.server.sound;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.platform.Services;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;


public class CCSounds {

    public static final Supplier<SoundEvent> GALLIAN_GROTTO = registerSoundEvents("gallian_grotto");
    public static final ResourceKey<JukeboxSong> GALLIAN_GROTTO_KEY = createSong("gallian_grotto");

    public static final Supplier<SoundEvent> PIGEON_IDLE = registerSoundEvents("pigeon_idle");
    public static final Supplier<SoundEvent> PIGEON_HURT = registerSoundEvents("pigeon_hurt");
    public static final Supplier<SoundEvent> PIGEON_DEATH = registerSoundEvents("pigeon_death");
    public static final Supplier<SoundEvent> PIGEON_FLAP = registerSoundEvents("pigeon_flap");
    public static final Supplier<SoundEvent> ENDOVE_IDLE = registerSoundEvents("endove_idle");
    public static final Supplier<SoundEvent> ENDOVE_HURT = registerSoundEvents("endove_hurt");
    public static final Supplier<SoundEvent> ENDOVE_DEATH = registerSoundEvents("endove_death");

    public static final Supplier<SoundEvent> COYOTE_IDLE = registerSoundEvents("coyote_idle");
    public static final Supplier<SoundEvent> COYOTE_HURT = registerSoundEvents("coyote_hurt");
    public static final Supplier<SoundEvent> COYOTE_DEATH = registerSoundEvents("coyote_death");

    public static final Supplier<SoundEvent> CARACARA_IDLE = registerSoundEvents("caracara_idle");
    public static final Supplier<SoundEvent> CARACARA_HURT = registerSoundEvents("caracara_hurt");
    public static final Supplier<SoundEvent> CARACARA_DEATH = registerSoundEvents("caracara_death");


    private static ResourceKey<JukeboxSong> createSong(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, name));
    }

    private static Supplier<SoundEvent> registerSoundEvents(String name) {
        Supplier<SoundEvent> sup = () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, name));
        sup = Suppliers.memoize(sup);
        Services.PLATFORM.register(BuiltInRegistries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, name)));
        return sup;
    }
}

