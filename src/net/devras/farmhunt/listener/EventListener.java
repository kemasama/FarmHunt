package net.devras.farmhunt.listener;

import java.sql.ResultSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import net.devras.farmhunt.Arena;
import net.devras.farmhunt.Farm;
import net.devras.farmhunt.Game;
import net.devras.farmhunt.MySQL;
import net.devras.farmhunt.util.ScoreHelper;

public class EventListener implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		event.setJoinMessage("");

		Arena.getInstance().Farms.put(p.getUniqueId(), new Farm(p, DisguiseType.CHICKEN, false));

		p.teleport(Arena.getInstance().getLobby());


		PlayerInventory inventory = p.getInventory();
		inventory.clear();
		inventory.setArmorContents(null);
		Game.Point.put(p.getUniqueId(), 0);

		p.setMaxHealth(20.0);
		p.setHealth(p.getMaxHealth());
		p.setGameMode(GameMode.ADVENTURE);

		if (MySQL.isConnected()) {
			try {
				ResultSet res = MySQL.query(String.format("select * from point where uuid='%s';", p.getUniqueId().toString()));
				while (res != null && res.next()) {
					Game.Point.put(p.getUniqueId(), res.getInt("point"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage("");

		Player p = event.getPlayer();

		if (ScoreHelper.hasScore(p)) {
			ScoreHelper.removeScore(p);
		}

		Arena.getInstance().Farms.remove(p.getUniqueId());

		if (MySQL.isConnected()) {
			try {
				//ResultSet res = Game.Instance.SQL.query(String.format("select * from point where uuid='%s';", p.getUniqueId().toString()));
				MySQL.update(String.format("insert into point(uuid, point) values('%s', '%s')"
						+ " on duplicate key update"
						+ " point=values(point);", p.getUniqueId().toString(), Game.Point.get(p.getUniqueId())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent event){
		Player p = event.getPlayer();
		String msg = event.getMessage();

		if (event.isCancelled()) {
			return;
		}


		event.setCancelled(true);

		if (!Game.Point.containsKey(p.getUniqueId())) {
			Game.Point.put(p.getUniqueId(), 0);
		}

		int point = Game.Point.get(p.getUniqueId());

		//String message = String.format("§6%s §b%s§7: §f%s", point, p.getName(), msg);
		String message = String.format("§6%s §b%s§7: §f%s", point, p.getName(), msg);

		Bukkit.broadcastMessage(message);
	}


	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		event.setDroppedExp(0);
		event.getDrops().clear();

		if (event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();

			if (p.getKiller() != null) {
				Game.addPoint(p.getKiller(), 10);
			}

			Bukkit.getScheduler().runTaskLater(Game.Instance, new Runnable() {
				@Override
				public void run() {
					p.spigot().respawn();

					Farm farm = Arena.getInstance().Farms.get(p.getUniqueId());
					farm.setSeeker(true);
					farm.Guise();
					farm.armor();

					p.teleport(Arena.getInstance().getSpawns().get((new Random()).nextInt(Arena.getInstance().getSpawns().size())));
				}
			}, 1L);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onFood(FoodLevelChangeEvent event) {
		event.setCancelled(true);
		event.setFoodLevel(20);
	}

	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerPickupItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

    @EventHandler
    public void onInventoryMove(InventoryClickEvent event) {
    	event.setCancelled(true);
    }
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		e.setCancelled(true);
		if (Arena.getInstance().isInGame) {
			if (e.getEntity() instanceof Player) {
				e.setCancelled(false);
			}
		}

		if (e.getEntityType().equals(EntityType.WOLF)) {
			e.setCancelled(true);
		}
		if (e.getEntityType().equals(EntityType.SHEEP)) {
			e.setCancelled(true);
		}
		if (e.getEntityType().equals(EntityType.PIG)) {
			e.setCancelled(true);
		}
		if (e.getEntityType().equals(EntityType.CHICKEN)) {
			e.setCancelled(true);
		}
		if (e.getEntityType().equals(EntityType.COW)) {
			e.setCancelled(true);
		}

	}
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		e.setCancelled(true);
		if (Arena.getInstance().isInGame) {
			if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
				// damaged
				Player p = (Player) e.getEntity();
				// damager
				Player k = (Player) e.getDamager();
				Farm pf = Arena.getInstance().Farms.get(p.getUniqueId());
				Farm kf = Arena.getInstance().Farms.get(k.getUniqueId());

				if (pf != null && kf != null) {

					// if damaged is as seeker
					if (pf.isSeeker()) {
						// enabled damage
						if (!kf.isSeeker()) {
							e.setCancelled(false);
						}else {
							e.setCancelled(true);
						}
						
					// if damaged is as hider
					}else {
						// if damager is as seeker
						if (kf.isSeeker()) {
							// enabled damage
							e.setCancelled(false);
						}else {
							e.setCancelled(true);
						}
					}
				}
				//e.setCancelled(false);
			}
		}
	}

	@EventHandler
	public void Interect(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();

		Farm farm = Arena.getInstance().Farms.get(p.getUniqueId());

		if (farm == null) {
			return;
		}

		if (!farm.isSeeker()) {
			farm.setAnimal(event.getRightClicked());
		}

		farm.Guise();

		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockFade(BlockFadeEvent event) {
		event.setCancelled(true);
	}
}
