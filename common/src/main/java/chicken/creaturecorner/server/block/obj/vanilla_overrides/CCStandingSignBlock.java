package chicken.creaturecorner.server.block.obj.vanilla_overrides;

import chicken.creaturecorner.server.blockentity.entity_obj.vanilla.CCSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;

public class CCStandingSignBlock extends StandingSignBlock {
    public CCStandingSignBlock(Properties properties, WoodType woodType) {
        super(woodType, properties);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new CCSignBlockEntity(pPos, pState);
    }
}
