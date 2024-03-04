package io.github.sjouwer.pickblockpro.util;

import io.github.sjouwer.pickblockpro.PickBlockPro;
import io.github.sjouwer.pickblockpro.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class DataComponentUtil {
    private static final ModConfig config = PickBlockPro.getConfig();
    private static final String ID_KEY = "id";
    private static final String COUNT_KEY = "count";
    private static final String LEVEL_KEY = "level";

    private DataComponentUtil() {
    }

    public static void setEntityData(ItemStack stack, Entity entity, boolean addLore) {
        NbtCompound entityCompound = getEntityNbt(entity);
        config.entityTagBlacklist().forEach(entityCompound::remove);
        if (entityCompound.isEmpty()) {
            return;
        }

        stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(entityCompound));

        if (addLore) {
            addLore(stack, "+Entity Data");
        }
    }

    public static NbtCompound getEntityNbt(Entity entity) {
        NbtCompound entityCompound = entity.writeNbt(new NbtCompound());

        if (entity instanceof Saddleable saddleable && saddleable.isSaddled()) {
            NbtCompound saddleCompound = new NbtCompound();
            saddleCompound.putString(ID_KEY, Items.SADDLE.toString());
            saddleCompound.putInt(COUNT_KEY, 1);
            entityCompound.put("SaddleItem", saddleCompound);
        }

        Identifier identifier = EntityType.getId(entity.getType());
        entityCompound.putString(ID_KEY, identifier.toString());

        return entityCompound;
    }

    public static void setBlockEntityData(ItemStack stack, BlockEntity blockEntity, DynamicRegistryManager registryManager, boolean addLore) {
        NbtCompound blockEntityCompound = blockEntity.createNbtWithIdentifyingData(registryManager);
        config.blockEntityTagBlacklist().forEach(blockEntityCompound::remove);
        if (blockEntityCompound.isEmpty()) {
            return;
        }

        BlockItem.setBlockEntityData(stack, blockEntity.getType(), blockEntityCompound);
        stack.applyComponentsFrom(blockEntity.createComponentMap());

        if (addLore) {
            addLore(stack, "+BlockEntity Data");
        }
    }

    public static void setBlockStateData(ItemStack stack, BlockState blockState, boolean addLore) {
        Map<String, String> properties = new HashMap<>();
        for (Property<?> property : blockState.getProperties()) {
            String key = property.getName();
            if (!config.blockStateTagBlacklist().contains(key)) {
                String value = blockState.get(property).toString();
                properties.put(key, value);
            }
        }

        if (properties.isEmpty()) {
            return;
        }

        stack.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(properties));

        if (addLore) {
            addLore(stack, "+BlockState Data");
        }
    }

    public static void addLore(ItemStack stack, String loreLine) {
        LoreComponent lore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        stack.set(DataComponentTypes.LORE, lore.of(Text.literal(loreLine)));
    }

    public static void setLightLevel(ItemStack light, int level) {
        BlockStateComponent state = light.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
        Map<String, String> properties = new HashMap<>(state.properties());
        properties.put(LEVEL_KEY, Integer.toString(level));

        light.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(properties));
    }

    public static void cycleLightLevel(ItemStack light) {
        BlockStateComponent state = light.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
        Map<String, String> properties = new HashMap<>(state.properties());

        if (properties.containsKey(LEVEL_KEY)) {
            int lightLvl = Integer.parseInt(properties.get(LEVEL_KEY));
            String newLightLvl = lightLvl == 15 ? "0" : String.valueOf(lightLvl + 1);
            properties.replace(LEVEL_KEY, newLightLvl);
        }
        else {
            properties.put(LEVEL_KEY, "0");
        }

        light.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(properties));
    }

    public static void setSkullOwner(ItemStack skull, PlayerEntity player) {
        ProfileComponent profile = new ProfileComponent(player.getGameProfile());
        skull.set(DataComponentTypes.PROFILE, profile);
    }

    public static int getAmountStored(ItemStack storage, Item item) {
        final int[] amount = {0};

        if (storage.contains(DataComponentTypes.CONTAINER)) {
            ContainerComponent container = storage.get(DataComponentTypes.CONTAINER);
            container.stream().forEach(stack -> {
                if (stack.getItem().equals(item)) {
                    amount[0] += stack.getCount();
                }
            });
        }

        if (storage.contains(DataComponentTypes.BUNDLE_CONTENTS)) {
            BundleContentsComponent bundleContents = storage.get(DataComponentTypes.BUNDLE_CONTENTS);
            bundleContents.stream().forEach(stack -> {
                if (stack.getItem().equals(item)) {
                    amount[0] += stack.getCount();
                }
            });
        }

        return amount[0];
    }
}
