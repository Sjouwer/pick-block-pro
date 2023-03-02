package io.github.sjouwer.pickblockpro.mixin;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DecorationItem.class)
public class DecorationItemMixin {

    //Remove the painting title from the tooltip if the itemStack's custom name is already the same title
    @Inject(method="appendTooltip", at=@At("TAIL"))
    public void removeTitleTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (tooltip.size() > 1 && stack.getName().getString().equals(tooltip.get(1).getString())) {
            tooltip.remove(1);
        }
    }
}
