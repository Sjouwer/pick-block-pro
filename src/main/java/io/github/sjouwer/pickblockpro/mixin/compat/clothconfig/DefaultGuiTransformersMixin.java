package io.github.sjouwer.pickblockpro.mixin.compat.clothconfig;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.util.EnchantmentUtil;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DefaultGuiTransformers.class)
public class DefaultGuiTransformersMixin {

    /**
     * Mixin to prevent having to add many of the same translations
     * This is needed because Auto Config doesn't allow specifying your own translation keys
     */
    @ModifyVariable(method = "tryApplyTooltip", at = @At("HEAD"), argsOnly = true)
    private static Text[] tryApplyCustomTooltip(Text[] text) {
        if (text.length > 0 && text[0].getContent() instanceof TranslatableTextContent translatable && translatable.getKey().contains(PickBlockPro.NAMESPACE)) {
            String key = translatable.getKey();
            if (key.endsWith("@Tooltip")) {
                key = key.substring(0, key.lastIndexOf('.'));
            }

            String fieldName = key.substring(key.lastIndexOf('.') + 1);
            Identifier id = Identifier.tryParse(fieldName.toLowerCase());
            if (EnchantmentUtil.isVanillaEnchantment(id) || fieldName.equals("item")) {
                String baseKey = key.substring(0, key.lastIndexOf('.'));
                Text toolOrWeapon = Text.translatable(baseKey);
                return new Text[]{ Text.translatable("text.autoconfig.pickblockpro/config.option.toolPicker." + fieldName + ".@Tooltip", toolOrWeapon) };
            }
        }

        return text;
    }
}
