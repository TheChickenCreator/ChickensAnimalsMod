package chicken.creaturecorner.server.sound;

import chicken.creaturecorner.AnimalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CCSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, AnimalMod.MODID);

    public static final Supplier<SoundEvent> GALLIAN_GROTTO = registerSoundEvents("gallian_grotto");
    public static final ResourceKey<JukeboxSong> GALLIAN_GROTTO_KEY = createSong("gallian_grotto");

    public static final Supplier<SoundEvent> PIGEON_IDLE = registerSoundEvents("pigeon_idle");
    public static final Supplier<SoundEvent> PIGEON_HURT = registerSoundEvents("pigeon_hurt");
    public static final Supplier<SoundEvent> PIGEON_DEATH = registerSoundEvents("pigeon_death");
    public static final Supplier<SoundEvent> PIGEON_FLAP = registerSoundEvents("pigeon_flap");

    public static final Supplier<SoundEvent> COYOTE_IDLE = registerSoundEvents("coyote_idle");
    public static final Supplier<SoundEvent> COYOTE_HURT = registerSoundEvents("coyote_hurt");
    public static final Supplier<SoundEvent> COYOTE_DEATH = registerSoundEvents("coyote_death");

    public static final Supplier<SoundEvent> CARACARA_IDLE = registerSoundEvents("caracara_idle");
    public static final Supplier<SoundEvent> CARACARA_HURT = registerSoundEvents("caracara_hurt");
    public static final Supplier<SoundEvent> CARACARA_DEATH = registerSoundEvents("caracara_death");


    private static ResourceKey<JukeboxSong> createSong(String name){
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, name));
    }

    private static Supplier<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
