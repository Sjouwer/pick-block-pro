package io.github.sjouwer.pickblockpro.mixin;

import io.github.sjouwer.pickblockpro.picker.BlockPicker;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void doItemPick(CallbackInfo info) {
        BlockPicker.getInstance().pickBlock();
        info.cancel();
    }
}
