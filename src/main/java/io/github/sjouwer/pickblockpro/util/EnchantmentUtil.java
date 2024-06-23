package io.github.sjouwer.pickblockpro.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.Optional;

public class EnchantmentUtil {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private EnchantmentUtil() {
    }

    public static RegistryEntry<Enchantment> getRegistryEntry(String name) {
        return getRegistryEntry(Identifier.tryParse(name));
    }

    public static RegistryEntry<Enchantment> getRegistryEntry(RegistryKey<Enchantment> key) {
        return getRegistryEntry(key.getValue());
    }

    public static RegistryEntry<Enchantment> getRegistryEntry(Identifier id) {
        if (client.world == null) {
            return null;
        }

        Registry<Enchantment> registry = client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        Optional<RegistryEntry.Reference<Enchantment>> enchantment = registry.getEntry(id);
        return enchantment.orElse(null);
    }

    public static boolean isVanillaEnchantment(Identifier id) {
        try {
            Field[] enchantments = Enchantments.class.getDeclaredFields();
            for(Field enchantment: enchantments) {
                if (enchantment.getType().equals(RegistryKey.class)) {
                    RegistryKey<?> key = (RegistryKey<?>) enchantment.get(Enchantments.class);
                    if (key.getValue().equals(id)) return true;
                }
            }
        }
        catch (Exception e) {
            return false;
        }

        return false;
    }
}
