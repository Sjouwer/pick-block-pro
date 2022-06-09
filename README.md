<img src="https://github.com/Sjouwer/pick-block-pro/blob/1.19/src/main/resources/assets/pickblockpro/icon.png" width="200">  

# **Pick Block Pro**

Pick Block: Get the block/item of what you're looking at in creative or switch to that block/item in survival.  
This mod is an advanced version of this and adds a lot of extra features (see below)

**Important:**  
This mod replaces the default Pick Block, however the ID and Tool picker use their own keybindings.  

## **Features**
Most things below are configurable in the config settings using the config file or Mod Menu
- In survival the block picker will now also search through item-containers like shulkers inside your inventory  
- Holding ALT while Block picking will add BlockState data as NBT to the block/item  
(stacks with CTRL for BlockEntity data)
- Holding CTRL while Entity picking will add Entity data as NBT to the item  
    - Copies mob data and adds it to the spawn egg  
    - Copies armor stands with their data  
    - Copies item frames including the framed item  
    - Copies painting variant  
- Pick extra things:  
    - Pick fire and get a flint & steel (1.16+)  
    - Pick the sun or moon and get a light block (1.17+)  
Cycle through the light levels by picking the sun or moon while holding the light block  
    - Pick a player and get the head of that player  
    - Pick mob spawners  
    - Pick fluids and get a bucket of the fluid  
- Extended Pick Block range, decide how far the picker should be able to reach
- Lock inventory slots to prevent the picker from switching or replacing the item in that slot
- Copy ID's of hotbar blocks/items directly into your chat, for easy WorldEdit command creation  
(Use CTRL + 0 through 9 while your chat is open, 0 is for the off hand slot)
- Pick ID's of blocks in the world with the ID Picker and copy them directly to your clipboard
- Use the Tool Picker and get the best available tool to break the block or kill the entity
- Creative inventory management (not just the hotbar) is now actually possible when picking blocks

## **Dependencies**
 		
**Required:**  
[Fabric API](https://github.com/FabricMC/fabric)  
[Cloth Config API Fabric](https://github.com/shedaniel/cloth-config) (Is required to make the config work)

**Optional:**  
[Mod Menu](https://github.com/TerraformersMC/ModMenu) (This mod allows you to edit the configs in game)
