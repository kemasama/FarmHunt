package net.devras.farmhunt;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import net.devras.farmhunt.util.ScoreHelper;

public class MainTask implements Runnable {

	public Arena arena;
	public MainTask(Arena arena) {
		this.arena = arena;
	}

	@Override
	public void run() {
		int time = 0;
		if (arena.isInGame) {
			arena.CRT_GAME_TIME--;
			time = arena.CRT_GAME_TIME;
			if (time < 1) {
				arena.Stop("§eHider Win");
			}
		}else {
			arena.CRT_LOBBY_TIME--;
			time = arena.CRT_LOBBY_TIME;
			if (time < 1) {
				arena.Start();
			}
		}

		boolean allSeeker = true;
		for (Player p : Bukkit.getOnlinePlayers()) {
			ScoreHelper helper;
			if (ScoreHelper.hasScore(p)) {
				helper = ScoreHelper.getByPlayer(p);
			}else {
				helper = ScoreHelper.createScore(p);
			}

			if (!arena.Farms.containsKey(p.getUniqueId())) {
				arena.Farms.put(p.getUniqueId(), new Farm(p, DisguiseType.CHICKEN, false));
			}
			Farm farm = arena.Farms.get(p.getUniqueId());
			farm.Guise();

			if (!farm.isSeeker()) {
				allSeeker = false;
			}

			helper.setTitle("§eFarm Hunt");

			helper.setSlot(9, "§7----------------");
			helper.setSlot(8, "§e残り時間");
			helper.setSlot(7, " §a" + time);
			helper.setSlot(6, "§eあなたは");
			helper.setSlot(5, " §c" + (farm.isSeeker() ? "SEEKER" : farm.getAnimal().name()));
			helper.setSlot(4, "§eポイント");
			helper.setSlot(3, " §e" + (Game.Point.containsKey(p.getUniqueId()) ? Game.Point.get(p.getUniqueId()) : 0));

			helper.setSlot(2, "§7----------------");

			helper.setSlot(1, "§emc.devras.info");

		}

		if (arena.isInGame && Bukkit.getOnlinePlayers().size() < 2) {
			arena.Stop();
		}
		if (arena.isInGame && allSeeker) {
			arena.Stop("§eSeeker Win");
		}

	}

}
