package ru.officialdakari.portalgunsf;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;

public class PortalGunPlugin extends JavaPlugin implements SlimefunAddon {

    Config config;

    static NamespacedKey itemMetaPortalGunId;
    static NamespacedKey itemMetaPortalGunColor1;
    static NamespacedKey itemMetaPortalGunColor2;
    static HashMap<String, ArrayList<PortalData>> portals = new HashMap();

    static HashMap<String, Entity> holding = new HashMap();
    static HashMap<String, BlockState> holdingBlock = new HashMap();

    static PortalGunPlugin plugin;

    static Boolean portalExists(Location loc) {
        for (ArrayList<PortalData> al : portals.values()) {
            for (PortalData pd : al) {
                if (loc.clone().add(0, -1, 0).distance(pd.location) == 0 || loc.clone().add(0, 1, 0).distance(pd.location) == 0 || pd.location.distance(loc) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    static Boolean isPortalPlaceable(Location loc, BlockFace bf) {
        if (portalExists(loc)) {
            return false;
        }
        if (bf == BlockFace.DOWN) {
            loc = loc.add(0, -1, 0);
            return loc.getBlock().getType() == Material.AIR;
        } else if (bf == BlockFace.UP) {
            loc = loc.add(0, 1, 0);
            return loc.getBlock().getType() == Material.AIR;
        } else if (bf == BlockFace.NORTH) {
            loc = loc.add(0, 0, -1);
            return loc.getBlock().getType() == Material.AIR && loc.add(0, 1, 0).getBlock().getType() == Material.AIR;
        } else if (bf == BlockFace.SOUTH) {
            loc = loc.add(0, 0, 1);
            return loc.getBlock().getType() == Material.AIR && loc.add(0, 1, 0).getBlock().getType() == Material.AIR;
        } else if (bf == BlockFace.WEST) {
            loc = loc.add(-1, 0, 0);
            return loc.getBlock().getType() == Material.AIR && loc.add(0, 1, 0).getBlock().getType() == Material.AIR;
        } else if (bf == BlockFace.EAST) {
            loc = loc.add(1, 0, 0);
            return loc.getBlock().getType() == Material.AIR && loc.add(0, 1, 0).getBlock().getType() == Material.AIR;
        }
        return false;
    }

    @Override
    public void onEnable() {
        config = new Config(this);

        plugin = this;

        itemMetaPortalGunId = new NamespacedKey(this, "unique_portals_id");
        itemMetaPortalGunColor1 = new NamespacedKey(this, "pg_1color");
        itemMetaPortalGunColor2 = new NamespacedKey(this, "pg_2color");

        NamespacedKey categoryId = new NamespacedKey(this, "portal_gun");
        ItemStack categoryItem = CustomItemStack.create(Material.DIAMOND, "&cPortal Gun", "The Cake is a Lie");

        ItemGroup itemGroup = new ItemGroup(categoryId, categoryItem);

        SlimefunItemStack portalGunItemStack = new SlimefunItemStack("PORTAL_GUN", Material.NETHERITE_HOE, "&aPortal Gun", "", "&7The Cake is a Lie");

        ItemStack[] portalGunRecipe = {
            new ItemStack(Material.BOW), new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.ENDER_PEARL),
            null, SlimefunItems.ELECTRO_MAGNET.asOne(), null,
            null, null, null
        };

        SlimefunItemStack gravityGunItemStack = new SlimefunItemStack("GRAVITY_GUN", Material.GOLDEN_HOE, "&bGravity Gun", "", "&7Grab blocks and entities");
        
        ItemStack[] gravityGunRecipe = {
            new ItemStack(Material.FISHING_ROD), new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.ENDER_PEARL),
            null, SlimefunItems.ELECTRO_MAGNET.asOne(), null,
            null, null, null
        };

        PortalGunItem pgItem = new PortalGunItem(itemGroup, portalGunItemStack, RecipeType.MAGIC_WORKBENCH, portalGunRecipe);
        pgItem.register(this);

        GravityGunItem ggItem = new GravityGunItem(itemGroup, gravityGunItemStack, RecipeType.MAGIC_WORKBENCH, gravityGunRecipe);
        ggItem.register(this);

        Bukkit.getPluginManager().registerEvents(new PortalGunEventListener(), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (String playerName : holding.keySet()) {
                Entity entity = holding.get(playerName);
                Player p = Bukkit.getPlayer(playerName);
                if (p == null) {
                    holding.remove(playerName);
                    continue;
                }
                RayTraceResult rtr = p.rayTraceBlocks(6, FluidCollisionMode.NEVER);
                if (rtr != null) {
                    Location loc = rtr.getHitPosition().toLocation(p.getWorld());
                    if (entity.getType() == EntityType.BLOCK_DISPLAY) loc.setDirection(new Vector(0, 0, 0));
                    entity.teleport(loc.add(0, 0.8, 0));
                } else {
                    Location loc = p.getLocation().clone().add(p.getLocation().getDirection().multiply(4));
                    if (entity.getType() == EntityType.BLOCK_DISPLAY) loc.setDirection(new Vector(0, 0, 0));
                    entity.teleport(loc.add(0, 0.8, 0));
                }
                entity.setFallDistance(0);
            }
        }, 5L, 5L);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (String key : portals.keySet()) {
                for (PortalData pd : portals.get(key)) {
                    if (pd.blockFace == BlockFace.UP) {
                        pd.location.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                pd.location.getBlockX() + 0.5,
                                pd.location.getBlockY() + 0.98,
                                pd.location.getBlockZ() + 0.5,
                                30,
                                0.2,
                                0,
                                0.2,
                                0,
                                new Particle.DustTransition(pd.color, pd.color, 1)
                        );
                    } else if (pd.blockFace == BlockFace.DOWN) {
                        pd.location.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                pd.location.getBlockX() + 0.5,
                                pd.location.getBlockY() - 0.02,
                                pd.location.getBlockZ() + 0.5,
                                30,
                                0.2,
                                0,
                                0.2,
                                0,
                                new Particle.DustTransition(pd.color, pd.color, 1)
                        );
                    } else if (pd.blockFace == BlockFace.NORTH) {
                        pd.location.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                pd.location.getBlockX() + 0.45,
                                pd.location.getBlockY() + 1,
                                pd.location.getBlockZ() - 0.02,
                                30,
                                0.2,
                                0.45,
                                0,
                                0,
                                new Particle.DustTransition(pd.color, pd.color, 1)
                        );
                    } else if (pd.blockFace == BlockFace.SOUTH) {
                        pd.location.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                pd.location.getBlockX() + 0.45,
                                pd.location.getBlockY() + 1,
                                pd.location.getBlockZ() + 1,
                                30,
                                0.2,
                                0.45,
                                0,
                                0,
                                new Particle.DustTransition(pd.color, pd.color, 1)
                        );
                    } else if (pd.blockFace == BlockFace.WEST) {
                        pd.location.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                pd.location.getBlockX() - 0.02,
                                pd.location.getBlockY() + 1,
                                pd.location.getBlockZ() + 0.45,
                                30,
                                0,
                                0.45,
                                0.2,
                                0,
                                new Particle.DustTransition(pd.color, pd.color, 1)
                        );
                    } else if (pd.blockFace == BlockFace.EAST) {
                        pd.location.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                pd.location.getBlockX() + 1,
                                pd.location.getBlockY() + 1,
                                pd.location.getBlockZ() + 0.45,
                                30,
                                0,
                                0.45,
                                0.2,
                                0,
                                new Particle.DustTransition(pd.color, pd.color, 1)
                        );
                    }
                }
            }
        }, 1L, 1L);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public JavaPlugin getJavaPlugin() {
        // This is a method that links your SlimefunAddon to your Plugin.
        // Just return "this" in this case, so they are linked
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        // Here you can return a link to your Bug Tracker.
        // This link will be displayed to Server Owners if there is an issue
        // with this Addon. Return null if you have no bug tracker.
        // Normally you can just use GitHub's Issues tab:
        // https://github.com/YOURNAME/YOURPROJECT/issues
        return null;
    }
}
