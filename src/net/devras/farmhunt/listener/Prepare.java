package net.devras.farmhunt.listener;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.devras.farmhunt.Arena;
import net.devras.farmhunt.Game;

public class Prepare implements Listener{
	private static HashMap<UUID, Boolean> Prepares = new HashMap<>();

	public static void prepareRun(final Player p) {
		Prepares.put(p.getUniqueId(), true);
		Bukkit.getScheduler().runTaskLater(Game.Instance, new Runnable() {
			@Override
			public void run() {
				// away to 20s
				Prepares.remove(p.getUniqueId());
				p.teleport(Arena.getInstance().getSpawns().get((new Random()).nextInt(Arena.getInstance().getSpawns().size())));
			}
		}, 20L * 20);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (event.getFrom().getBlockX() != event.getTo().getBlockX() && event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
			if (Prepares.containsKey(event.getPlayer().getUniqueId()) && Prepares.get(event.getPlayer().getUniqueId())) {
				event.getPlayer().teleport(Arena.getInstance().getLobby());
			}
		}
	}
}
