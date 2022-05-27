package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class Raycast {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private Raycast() {
    }

    /**
     * Raycast from the camera (player's eyes) forward
     * @param range How far to cast in blocks
     * @param ignoreFluids Should the cast ignore fluids, making it pass through it
     * @param ignoreEntities Should the cast ignore entities, making it pass through them
     * @return Result of the raycast
     */
    public static HitResult getHit(int range, boolean ignoreFluids, boolean ignoreEntities) {
        if (client.cameraEntity == null || client.world == null) {
            PickBlockPro.LOGGER.error("Tried to raycast outside of play; no world and/or camera");
            return null;
        }

        RaycastContext.FluidHandling fluidHandling = ignoreFluids ? RaycastContext.FluidHandling.NONE : RaycastContext.FluidHandling.ANY;
        Entity player = client.cameraEntity;
        Vec3d vector = player.getRotationVec(client.getTickDelta());
        Vec3d rayStart = player.getCameraPosVec(client.getTickDelta());
        Vec3d rayEnd = rayStart.add(vector.multiply(range));
        BlockHitResult blockHit = client.world.raycast(new RaycastContext(rayStart, rayEnd, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));

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
