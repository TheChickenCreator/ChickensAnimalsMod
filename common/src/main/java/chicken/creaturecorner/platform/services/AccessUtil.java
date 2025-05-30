package chicken.creaturecorner.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AccessUtil {
    public interface BlockEntitySupplier<T extends BlockEntity> {
        T create(BlockPos var1, BlockState var2);
    }

    public interface MenuSupplier<T extends AbstractContainerMenu> {
        T create(int var1, Inventory var2);
    }
}
