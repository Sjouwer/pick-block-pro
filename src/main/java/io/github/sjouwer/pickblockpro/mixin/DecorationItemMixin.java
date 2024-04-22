package io.github.sjouwer.pickblockpro.mixin;

import net.minecraft.client.item.TooltipType;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DecorationItem.class)
public class DecorationItemMixin {

    //Remove the painting title from the tooltip if the itemStack's custom name is already the same title
    @Inject(method="appendTooltip", at=@At("TAIL"))
    public void removeTitleTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if (tooltip.size() > 1 && stack.getName().getString().equals(tooltip.get(1).getString())) {
            tooltip.remove(1);
        }
    }
}
