package io.github.sjouwer.pickblockpro.mixin;

import io.github.sjouwer.pickblockpro.picker.BlockPicker;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    /**
     * @author
     * @reason PBP makes too many changes to be able to mixin into the vanilla Pick Block
     */
    @Overwrite
    private void doItemPick() {
        BlockPicker.pickBlock();
    }
}
