package io.github.sjouwer.pickblockpro.mixin;

import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.predicate.entity.EntityPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(EntityPropertiesLootCondition.class)
public interface EntityPropertiesLootConditionAccessor {
    @Accessor
    Optional<EntityPredicate> getPredicate();
}
