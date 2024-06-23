package io.github.sjouwer.pickblockpro.mixin.compat.clothconfig;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.util.EnchantmentUtil;
import me.shedaniel.clothconfig2.impl.builders.FieldBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FieldBuilder.class)
public class FieldBuilderMixin {
    @Shadow
    @Final
    private Text fieldNameKey;

    /**
     * Mixin to prevent having to add many of the same translations
     * This is needed because Auto Config doesn't allow specifying your own translation keys
     * For enchantments it'll now use the default minecraft translation
     */
    @Inject(method = "getFieldNameKey", at = @At("HEAD"), cancellable = true)
    public final void returnMinecraftTranslationKey(CallbackInfoReturnable<Text> info) {
        if (fieldNameKey.getContent() instanceof TranslatableTextContent translatable && translatable.getKey().contains(PickBlockPro.NAMESPACE)) {
            String key = translatable.getKey();
            String fieldName = key.substring(key.lastIndexOf('.') + 1);
            if (fieldName.equals("item")) {
                info.setReturnValue(Text.translatable("text.autoconfig.pickblockpro/config.option.toolPicker.item"));
            }

            Identifier id = Identifier.tryParse(fieldName.toLowerCase());
            if (EnchantmentUtil.isVanillaEnchantment(id)) {
                info.setReturnValue(Text.translatable("enchantment.minecraft." + fieldName));
            }
        }
    }
}
