package io.github.sjouwer.pickblockpro.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sjouwer.pickblockpro.PickBlockPro;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PickBlockOverrides {
    private static final HashMap<Block, ItemStack> blockOverrides = new HashMap<>();
    private static final HashMap<EntityType<?>, ItemStack> entityOverrides = new HashMap<>();
    private static final Gson gson = new Gson();
    private static int errors = 0;

    private PickBlockOverrides() {
    }

    public static ItemStack getBlockOverride(Block block) {
        if (blockOverrides.containsKey(block)) {
            return blockOverrides.get(block).copy();
        }
        return null;
    }

    public static ItemStack getEntityOverride(EntityType<?> entity) {
        if (entityOverrides.containsKey(entity)) {
            return entityOverrides.get(entity).copy();
        }
        return null;
    }

    public static void parseOverrides() {
        File file = FileHandler.getOverridesFile();
        if (!file.exists()) {
            PickBlockPro.LOGGER.warn("Failed to load \"" + file.getName() + "\" because it doesn't exist (anymore)");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) {
                PickBlockPro.LOGGER.warn("\"" + file.getName() + "\" doesn't appear to be an actual json and could not be loaded");
                return;
            }

            Map<String, JsonElement> objectMap = new HashMap<>();
            rootElement.getAsJsonObject().entrySet().forEach(s -> objectMap.put(s.getKey(), s.getValue()));
            JsonElement blockOverridesElement = objectMap.get("Block to ItemStack");
            if (blockOverridesElement != null) {
                parseBlockOverrides(blockOverridesElement);
            }
            else {
                PickBlockPro.LOGGER.info("No Block Overrides found");
            }

            JsonElement entityOverridesElement = objectMap.get("Entity to ItemStack");
            if (entityOverridesElement != null) {
                parseEntityOverrides(entityOverridesElement);
            }
            else {
                PickBlockPro.LOGGER.info("No Entity Overrides found");
            }
        }
        catch (JsonSyntaxException e) {
            PickBlockPro.LOGGER.warn("\"" + file.getName() + "\" is not properly formatted and could not be loaded: " + e.getCause().getMessage());
        }
        catch (IOException e) {
            PickBlockPro.LOGGER.error("Failed to load \"" + file.getName() + "\"");
            e.printStackTrace();
        }
    }

    private static void parseBlockOverrides(JsonElement blockOverridesElement) {
        blockOverrides.clear();
        errors = 0;
        LinkedHashMap<String, String> overrideMap = gson.fromJson(blockOverridesElement, new TypeToken<LinkedHashMap<String, String>>(){}.getType());
        for (Map.Entry<String, String> entry : overrideMap.entrySet()) {
            Block block = idToBlock(entry.getKey());
            ItemStack stack = idToItemStack(entry.getValue());
            if (block != null && stack != null) {
                blockOverrides.put(block, stack);
            }
        }

        int count = blockOverrides.size();
        PickBlockPro.LOGGER.info("Loaded " + count + " block override" + (count == 1 ? "" : "s") + " and encountered " + errors + " error" + (errors == 1 ? "" : "s"));
    }

    private static void parseEntityOverrides(JsonElement entityOverridesElement) {
        entityOverrides.clear();
        errors = 0;
        LinkedHashMap<String, String> overrideMap = gson.fromJson(entityOverridesElement, new TypeToken<LinkedHashMap<String, String>>(){}.getType());
        for (Map.Entry<String, String> entry : overrideMap.entrySet()) {
            EntityType<?> entity = idToEntity(entry.getKey());
            ItemStack stack = idToItemStack(entry.getValue());
            if (entity != null && stack != null) {
                entityOverrides.put(entity, stack);
            }
        }

        int count = entityOverrides.size();
        PickBlockPro.LOGGER.info("Loaded " + count + " entity override" + (count == 1 ? "" : "s") + " and encountered " + errors + " error" + (errors == 1 ? "" : "s"));
    }

    private static Block idToBlock(String id) {
        Identifier identifier = stringToId(id);
        Block block = Registry.BLOCK.get(identifier);
        if (block.equals(Blocks.AIR)) {
            PickBlockPro.LOGGER.warn("Failed to parse Block ID: " + id);
            errors++;
            return null;
        }
        return block;
    }

    private static EntityType<?> idToEntity(String id) {
        Optional<EntityType<?>> entity = EntityType.get(id);
        if (entity.isPresent()) {
            return entity.get();
        }
        else {
            PickBlockPro.LOGGER.warn("Failed to parse Entity ID: " + id);
            errors++;
            return null;
        }
    }

    private static ItemStack idToItemStack(String id) {
        String itemId = id;
        String itemNbt = "";
        if (id.contains("{")) {
            itemId = id.substring(0, id.indexOf("{"));
            itemNbt = id.substring(id.indexOf("{"));
        }

        Identifier identifier = stringToId(itemId);
        Item item = Registry.ITEM.get(identifier);
        if (item.equals(Items.AIR)) {
            PickBlockPro.LOGGER.warn("Failed to parse Item ID: " + itemId);
            errors++;
            return null;
        }

        ItemStack stack = new ItemStack(item);
        if (!itemNbt.isEmpty()) {
            try {
                stack.setNbt(StringNbtReader.parse(itemNbt));
            }
            catch (CommandSyntaxException e) {
                PickBlockPro.LOGGER.warn("Failed to parse NBT data: " + itemNbt);
                errors++;
            }
        }

        return stack;
    }

    private static Identifier stringToId(String id) {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) {
            return new Identifier("minecraft:air");
        }
        return identifier;
    }
}