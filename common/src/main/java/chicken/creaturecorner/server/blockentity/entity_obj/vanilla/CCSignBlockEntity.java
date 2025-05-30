package chicken.creaturecorner.server.blockentity.entity_obj.vanilla;

import chicken.creaturecorner.server.blockentity.CCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CCSignBlockEntity extends SignBlockEntity {
    public CCSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(CCBlockEntities.SIGN.get(), pPos, pBlockState);
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return CCBlockEntities.SIGN.get();
    }
}
