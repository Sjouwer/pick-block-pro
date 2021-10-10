package io.github.sjouwer.pickblockpro.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class Raycast {
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    private Raycast() {
    }

    public static HitResult getHit(int range, RaycastContext.FluidHandling fluidHandling, boolean ignoreEntities) {
        Entity player = minecraft.cameraEntity;
        Vec3d vector = player.getRotationVec(minecraft.getTickDelta());
        Vec3d rayStart = player.getCameraPosVec(minecraft.getTickDelta());
        Vec3d rayEnd = rayStart.add(vector.multiply(range));
        BlockHitResult blockHit = minecraft.world.raycast(new RaycastContext(rayStart, rayEnd, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));

        if (ignoreEntities) {
            return blockHit;
        }

        Box box = player.getBoundingBox().stretch(vector.multiply(range));
        int range2 = range * range;
        EntityHitResult entityHit = ProjectileUtil.raycast(player, rayStart, rayEnd, box, entityX -> !entityX.isSpectator() && entityX.collides(), range2);

        if (entityHit == null) {
            return blockHit;
        }

        if (blockHit.getPos().squaredDistanceTo(player.getPos()) < entityHit.getPos().squaredDistanceTo(player.getPos())) {
            return blockHit;
        }

        return entityHit;
    }
}
