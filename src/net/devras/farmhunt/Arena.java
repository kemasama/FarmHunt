package net.devras.farmhunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.devras.farmhunt.listener.EmeraldItem;
import net.devras.farmhunt.listener.Prepare;
import net.devras.farmhunt.util.Title;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class Arena {
	public static Arena Instance;
	public static Arena getInstance() { return Instance; }
	public static int INT_GAME_TIME, INT_LOBBY_TIME;

	public Arena() {
		Instance = this;
	}

	public HashMap<UUID, Farm> Farms = new HashMap<>();
	private Location lobby, seekerSpawn;
	private ArrayList<Location> Spawns = new ArrayList<>();

	public boolean isInGame = false;
	public boolean suddenDeath = false;

	public int CRT_GAME_TIME = 0;
	public int CRT_LOBBY_TIME = 60;

	public int type = 0;

	public void Start() {
		CRT_LOBBY_TIME = INT_LOBBY_TIME;
		CRT_GAME_TIME = INT_GAME_TIME;
		if (Bukkit.getOnlinePlayers().size() < 2) {
			//Bukkit.broadcastMessage("§e人数が足りないため、§eゲームを開始できません。");
			Bukkit.broadcastMessage("§eゲームを始めるには、あと一人必要です。");
			return;
		}

		isInGame = true;
		suddenDeath = false;

		// Choose seeker
		Random r = new Random();
		boolean seeker = false;
		do {
			Player p = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
			if (r.nextBoolean()) {
				Farm farm = Farms.get(p.getUniqueId());
				if (farm.isSeeker()) {
					seeker = true;
					p.sendMessage("§cあなたはシーカーです");
					break;
				}
				farm.setSeeker(true);
				farm.armor();
				Farms.put(p.getUniqueId(), farm);
				seeker = true;
				p.sendMessage("§cあなたはシーカーです");
				break;
			}
		} while (!seeker);

		EntityType[] types = new EntityType[]{
			EntityType.CHICKEN,
			EntityType.PIG,
			EntityType.COW,
			EntityType.HORSE,
			EntityType.SHEEP
		};

		for (Entity ent : seekerSpawn.getWorld().getEntities()) {
			if (ent.getType().equals(EntityType.DROPPED_ITEM)) {
				ent.remove();
				continue;
			}

			if (ent instanceof Player) {
				continue;
			}
			if (ent instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) ent;
				if (!entity.isDead() && entity.getHealth() > 0) {
					Location loc = ent.getLocation();
					ent.remove();

					entity = (LivingEntity) loc.getWorld().spawnEntity(loc, types[r.nextInt(types.length)]);

					try {
						net.minecraft.server.v1_8_R3.Entity nmsEn = ((CraftEntity) entity).getHandle();

						NBTTagCompound compound = new NBTTagCompound();
						nmsEn.c(compound);
						compound.setByte("NoAI", (byte) 1);
						nmsEn.f(compound);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}

		Title t = new Title("§eGame Start", "§eGood Luck");
		for (Player p : Bukkit.getOnlinePlayers()) {
			t.send(p);

			if (!Farms.get(p.getUniqueId()).isSeeker()) {
				p.getInventory().clear();
				p.getInventory().setArmorContents(null);
				p.getInventory().setItem(4, EmeraldItem.emerald);
				p.teleport(Spawns.get(r.nextInt(Spawns.size())));
			}else {
				p.teleport(seekerSpawn);
				Prepare.prepareRun(p);
				CRT_GAME_TIME = INT_GAME_TIME + 20;
			}

			p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
		}

		Bukkit.broadcastMessage("§eゲームスタート！");
	}

	public void Stop() {
		Stop("Drow");
	}
	public void Stop(String sub) {
		isInGame = false;
		suddenDeath = false;
		CRT_LOBBY_TIME = INT_LOBBY_TIME;
		CRT_GAME_TIME = INT_GAME_TIME;

		System.out.println(isInGame + "/" + suddenDeath + "/" + CRT_LOBBY_TIME + "/" + CRT_GAME_TIME);
		Bukkit.broadcastMessage("§eゲームオーバー！");

		Title t = new Title("§cGame Over", sub);
		for (Player p : Bukkit.getOnlinePlayers()) {
			t.send(p);
			Game.sendLobby(p);
		}

		Farms.clear();

	}

	public void addSpawn(Location spawn) {
		Spawns.add(spawn);
	}

	public Location getLobby() {
		return lobby;
	}

	public void setLobby(Location lobby) {
		this.lobby = lobby;
	}

	public Location getSeekerSpawn() {
		return seekerSpawn;
	}

	public void setSeekerSpawn(Location loc) {
		this.seekerSpawn = loc;
	}

	public ArrayList<Location> getSpawns() {
		return Spawns;
	}

	public void setSpawns(ArrayList<Location> spawns) {
		Spawns = spawns;
	}

}
