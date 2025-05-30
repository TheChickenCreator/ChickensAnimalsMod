package chicken.creaturecorner.server.blockentity.entity_obj.vanilla;

import chicken.creaturecorner.server.blockentity.CCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CCHangingSignBlockEntity extends SignBlockEntity {
    public CCHangingSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(CCBlockEntities.HANGING_SIGN.get(), pPos, pBlockState);
    }

    public int getTextLineHeight() {
        return 9;
    }

    public int getMaxTextLineWidth() {
        return 60;
    }

    public @NotNull SoundEvent getSignInteractionFailedSoundEvent() {
        return SoundEvents.WAXED_HANGING_SIGN_INTERACT_FAIL;
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return CCBlockEntities.HANGING_SIGN.get();
    }
}
