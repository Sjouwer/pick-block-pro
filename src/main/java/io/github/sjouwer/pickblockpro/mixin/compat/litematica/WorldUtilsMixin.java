package io.github.sjouwer.pickblockpro.mixin.compat.litematica;

import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import io.github.sjouwer.pickblockpro.util.InventoryManager;
import io.github.sjouwer.pickblockpro.util.NbtUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldUtils.class)
public class WorldUtilsMixin {
    @Inject(method = "doSchematicWorldPickBlock", at = @At("HEAD"), cancellable = true)
    private static void overrideLitematicaPickBlock(boolean closest, MinecraftClient mc, CallbackInfoReturnable<Boolean> info)
    {
        ModConfig config = PickBlockPro.getConfig();
        if(config.overrideLitematica()) {
            boolean isCreative = mc.player.getAbilities().creativeMode;
            BlockPos pos;
            if (closest) {
                pos = RayTraceUtils.getSchematicWorldTraceIfClosest(mc.world, mc.player, config.blockPickRange(isCreative));
            }
            else {
                pos = RayTraceUtils.getFurthestSchematicWorldBlockBeforeVanilla(mc.world, mc.player, config.blockPickRange(isCreative), true);
            }

            if (pos != null) {
                World world = SchematicWorldHandler.getSchematicWorld();
                BlockState state = world.getBlockState(pos);
                ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(state, world, pos);

                if (!stack.isEmpty()) {
                    if (isCreative) {
                        if (Screen.hasControlDown() && state.hasBlockEntity()) {
                            BlockEntity blockEntity = world.getBlockEntity(pos);
                            NbtUtil.addBlockEntityNbt(stack, blockEntity, true);
                        }
                        if (Screen.hasAltDown()) {
                            NbtUtil.addBlockStateNbt(stack, state, true);
                        }
                    }

                    InventoryManager.pickOrPlaceItemInInventory(stack);
                }

                info.setReturnValue(true);
                return;
            }

            info.setReturnValue(false);
        }
    }
}
