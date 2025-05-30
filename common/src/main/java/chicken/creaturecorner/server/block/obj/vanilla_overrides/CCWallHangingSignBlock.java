package chicken.creaturecorner.server.block.obj.vanilla_overrides;

import chicken.creaturecorner.server.blockentity.entity_obj.vanilla.CCHangingSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;

public class CCWallHangingSignBlock extends WallHangingSignBlock {
    public CCWallHangingSignBlock(Properties pProperties, WoodType pType) {
        super(pType, pProperties);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new CCHangingSignBlockEntity(pPos, pState);
    }
}
