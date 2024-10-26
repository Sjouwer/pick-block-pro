package io.github.sjouwer.pickblockpro.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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

        Optional<Registry<Enchantment>> registry = client.world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT);
        if (registry.isEmpty()) {
            return null;
        }

        Optional<RegistryEntry.Reference<Enchantment>> enchantment = registry.get().getEntry(id);
        return enchantment.orElse(null);
    }

    public static int getLevel(RegistryKey<Enchantment> enchantmentKey, ItemStack stack) {
        return EnchantmentHelper.getLevel(getRegistryEntry(enchantmentKey), stack);
    }

    public static int getLevel(ItemEnchantmentsComponent.Builder enchantmentsBuilder, RegistryKey<Enchantment> key) {
        return enchantmentsBuilder.getLevel(getRegistryEntry(key));
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

    public static double getItemEnchantmentsTotalDamage(ItemStack stack, EntityType<?> entityType) {
        List<Double> damage = new ArrayList<>();
        EnchantmentHelper
                .getEnchantments(stack)
                .getEnchantments()
                .forEach(en -> en.value().getEffect(EnchantmentEffectComponentTypes.DAMAGE)
                .forEach(ef -> damage.add(getEffectDamage(ef, EnchantmentHelper.getLevel(en, stack), entityType))));

        return damage.stream().reduce(0d, Double::sum);
    }

    public static double getEffectDamage(EnchantmentEffectEntry<EnchantmentValueEffect> effectEntry, int level, EntityType<?> entityType) {
        float effectDamage = effectEntry.effect().apply(level, Random.create(), 0);

        if (effectEntry.requirements().isPresent() && effectEntry.requirements().get() instanceof EntityPropertiesLootCondition condition) {
            if (condition.predicate().isPresent()) {
                EntityPredicate predicate = condition.predicate().get();
                if (predicate.type().isPresent()) {
                    return predicate.type().get().matches(entityType) ? effectDamage : 0;
                }
            }
        }

        return effectDamage;
    }
}
