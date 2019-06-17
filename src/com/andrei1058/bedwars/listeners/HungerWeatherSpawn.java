package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.GameState;
import com.andrei1058.bedwars.api.ServerType;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.andrei1058.bedwars.Main.*;

public class HungerWeatherSpawn implements Listener {

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        switch (getServerType()) {
            case SHARED:
                if (Arena.getArenaByPlayer((Player) e.getEntity()) != null) {
                    e.setCancelled(true);
                }
                break;
            default:
                e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (e.toWeatherState()) {
            switch (getServerType()) {
                case SHARED:
                    if (Arena.getArenaByName(e.getWorld().getName()) != null) {
                        e.setCancelled(true);
                    }
                    break;
                default:
                    e.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    //Used to prevent creature spawn
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            if (getServerType() == ServerType.SHARED) {
                if (Arena.getArenaByName(e.getEntity().getWorld().getName()) != null) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent e) {
        Arena a = Arena.getArenaByPlayer(e.getPlayer());
        if (a == null) return;
        /* remove empty bottle */
        switch (e.getItem().getType()) {
            case POTION:
                Bukkit.getScheduler().runTaskLater(plugin, () -> nms.minusAmount(e.getPlayer(), new ItemStack(Material.GLASS_BOTTLE), 1), 5L);
                PotionMeta pm = (PotionMeta) e.getItem().getItemMeta();
                if (pm.hasCustomEffects()) {
                    if (pm.hasCustomEffect(PotionEffectType.INVISIBILITY)) {
                        for (Player p1 : e.getPlayer().getWorld().getPlayers()) {
                            nms.hideArmor(e.getPlayer(), p1);
                        }
                        for (PotionEffect pe : pm.getCustomEffects()) {
                                a.getShowTime().put(e.getPlayer(), pe.getDuration() / 20);
                                break;
                        }
                    }
                }
                break;
            case GLASS_BOTTLE:
                nms.minusAmount(e.getPlayer(), nms.getItemInHand(e.getPlayer()), 1);
                break;
            case MILK_BUCKET:
                e.setCancelled(true);
                nms.minusAmount(e.getPlayer(), nms.getItemInHand(e.getPlayer()), 1);
                int task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Arena.magicMilk.remove(e.getPlayer().getUniqueId());
                    debug("PlayerItemConsumeEvent player "+ e.getPlayer() + " was removed from magicMilk");
                }, 20*30L).getTaskId();
                Arena.magicMilk.put(e.getPlayer().getUniqueId(), task);
                break;
        }
    }

    /*@EventHandler
    public void he(EntityRegainHealthEvent e){
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Arena a = Arena.getArenaByPlayer(p);
            if (a != null) {
                if (a.isPlayer(p)) {
                    BedWarsTeam t = a.getTeam(p);
                    if (t != null) {
                        p.setPlayerListName(lang.m(lang.tablistFormat).replace("{TeamColor}", TeamColor.getChatColor(t.getColor()).toString()).replace("{TeamLetter}", t.getName().substring(0, 1).toUpperCase())
                                .replace("{TeamName}", t.getName()).replace("{PlayerName}", p.getName()).replace("{PlayerHealth}", String.valueOf((int) p.getHealth())));
                    }
                }
            }
        }
    }*/

    @EventHandler
    //Prevent item spawning, issue #60
    public void onItemSpawn(ItemSpawnEvent e){
        Location l = e.getEntity().getLocation();
        Arena a = Arena.getArenaByName(l.getWorld().getName());
        if (a == null) return;
        if (a.getStatus() != GameState.playing){
            e.setCancelled(true);
        }
    }
}