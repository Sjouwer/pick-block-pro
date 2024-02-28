package io.github.sjouwer.pickblockpro.mixin;

import io.github.sjouwer.pickblockpro.picker.BlockPicker;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    /**
     *  Unconditional cancel at HEAD can't be avoided
     *  PBP makes too many changes to be able to mixin into the vanilla Pick Block
     *  Override is also not possible because of conflict with Fabric API
     *  However, the following Fabric API events have also been implemented into PBP:
     *  - ClientPickBlockGatherCallback
     *      Right after raycast and before stack manipulation
     *  - ClientPickBlockApplyCallback
     *      Right after stack manipulation and before inventory management
     */
    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void doItemPick(CallbackInfo info) {
        BlockPicker.pickBlock();
        info.cancel();
    }
}
