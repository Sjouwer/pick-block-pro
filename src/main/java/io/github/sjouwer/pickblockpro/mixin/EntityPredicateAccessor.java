package io.github.sjouwer.pickblockpro.mixin;

import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(EntityPredicate.class)
public interface EntityPredicateAccessor {
    @Accessor
    Optional<EntityTypePredicate> getType();
}
