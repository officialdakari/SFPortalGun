package ru.officialdakari.portalgunsf;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemDropHandler;

public class PortalGunItem extends SlimefunItem {

    public PortalGunItem(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        ItemUseHandler useHandler = this::onItemUse;
        ItemDropHandler dropHandler = this::onPgItemDrop;

        addItemHandler(useHandler);
        addItemHandler(dropHandler);
    }

    private String getPortalGunId(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        PersistentDataContainer pdc = im.getPersistentDataContainer();
        String id;
        if (!pdc.has(PortalGunPlugin.itemMetaPortalGunId, PersistentDataType.STRING)) {
            id = UUID.randomUUID().toString();
            pdc.set(PortalGunPlugin.itemMetaPortalGunId, PersistentDataType.STRING, id);
            itemStack.setItemMeta(im);
        } else {
            id = pdc.get(PortalGunPlugin.itemMetaPortalGunId, PersistentDataType.STRING);
        }
        return id;
    }

    private String getPortal1Color(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        PersistentDataContainer pdc = im.getPersistentDataContainer();
        String color;
        if (!pdc.has(PortalGunPlugin.itemMetaPortalGunColor1, PersistentDataType.STRING)) {
            color = String.valueOf((int) Math.floor(Math.random() * 255)) + ";" + String.valueOf((int) Math.floor(Math.random() * 255)) + ";" + String.valueOf((int) Math.floor(Math.random() * 255));
            pdc.set(PortalGunPlugin.itemMetaPortalGunColor1, PersistentDataType.STRING, color);
            itemStack.setItemMeta(im);
        } else {
            color = pdc.get(PortalGunPlugin.itemMetaPortalGunColor1, PersistentDataType.STRING);
        }

        return color;
    }

    private String getPortal2Color(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        PersistentDataContainer pdc = im.getPersistentDataContainer();
        String color;
        if (!pdc.has(PortalGunPlugin.itemMetaPortalGunColor2, PersistentDataType.STRING)) {
            color = String.valueOf((int) Math.floor(255 - Math.random() * 255)) + ";" + String.valueOf((int) Math.floor(255 - Math.random() * 255)) + ";" + String.valueOf((int) Math.floor(255 - Math.random() * 255));
            pdc.set(PortalGunPlugin.itemMetaPortalGunColor2, PersistentDataType.STRING, color);
            itemStack.setItemMeta(im);
        } else {
            color = pdc.get(PortalGunPlugin.itemMetaPortalGunColor2, PersistentDataType.STRING);
        }

        return color;
        // String[] s = color.split(";");
        // return Color.fromRGB(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    }

    private boolean onPgItemDrop(PlayerDropItemEvent event, Player p, Item item) {
        if (!p.isSneaking()) {
            ItemStack itemStack = item.getItemStack();
            ItemMeta im = itemStack.getItemMeta();
            PersistentDataContainer pdc = im.getPersistentDataContainer();
            if (!pdc.has(PortalGunPlugin.itemMetaPortalGunId, PersistentDataType.STRING)) {
                return false;
            }
            event.setCancelled(true);
            PortalGunPlugin.portals.remove(pdc.get(PortalGunPlugin.itemMetaPortalGunId, PersistentDataType.STRING));
            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 1);
        }
        return false;
    }

    private void onItemUse(PlayerRightClickEvent event) {
        event.cancel();

        Player p = event.getPlayer();
        Location loc = p.getLocation().clone().add(0, 1.5, 0);
        loc = loc.add(loc.getDirection().multiply(1));
        Snowball ball = (Snowball) p.getWorld().spawnEntity(loc, EntityType.SNOWBALL);
        ball.setGravity(false);
        ball.setBounce(false);
        ball.setGlowing(true);
        ball.setVelocity(loc.getDirection().multiply(4));
        ball.setMetadata("portalgunball", new FixedMetadataValue(addon.getJavaPlugin(), p.getName()));

        p.playSound(loc, Sound.ENTITY_ENDER_PEARL_THROW, 1, 1);

        String pgId = getPortalGunId(event.getItem());
        ball.setMetadata("portalgunid", new FixedMetadataValue(addon.getJavaPlugin(), pgId));
        ball.setMetadata("color_1", new FixedMetadataValue(addon.getJavaPlugin(), getPortal1Color(event.getItem())));
        ball.setMetadata("color_2", new FixedMetadataValue(addon.getJavaPlugin(), getPortal2Color(event.getItem())));
    }

}
