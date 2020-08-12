package net.devras.farmhunt;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.devras.farmhunt.listener.EmeraldItem;
import net.devras.farmhunt.listener.EventListener;
import net.devras.farmhunt.listener.Prepare;

public class Game extends JavaPlugin {
	public static Game Instance;

	public static HashMap<UUID, Integer> Point = new HashMap<>();
	public FileConfiguration config;

	public static void addPoint(Player p, int amount) {
		int point = Point.get(p.getUniqueId());
		point += amount;
		Point.put(p.getUniqueId(), point);
	}

	@Override
	public void onDisable() {

		config.set("INT_LOBBY_TIME", Arena.INT_LOBBY_TIME);
		config.set("INT_GAME_TIME", Arena.INT_GAME_TIME);
		config.set("lobby", Arena.getInstance().getLobby());
		config.set("seeker", Arena.getInstance().getSeekerSpawn());
		config.set("spawns", Arena.getInstance().getSpawns());

		saveConfig();

		super.onDisable();
	}

	@Override
	public void onEnable() {

		/**
		 * set Instance
		 */

		Instance = this;
		Arena arena = new Arena();

		/**
		 * Config
		 */
		saveDefaultConfig();

		config = getConfig();

		try {
			Arena.INT_LOBBY_TIME = config.getInt("INT_LOBBY_TIME", 60);
			Arena.INT_GAME_TIME = config.getInt("INT_GAME_TIME", 180);

			if (config.get("lobby") instanceof Location) {
				arena.setLobby((Location) config.get("lobby"));
			}

			if (config.get("seeker") instanceof Location) {
				arena.setSeekerSpawn((Location) config.get("seeker"));
			}


			if (config.contains("spawns")) {
				for (Object obj : config.getList("spawns")) {
					if (obj instanceof Location) {
						arena.addSpawn((Location) obj);
					}
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}


		try {
			MySQL.Config(config.getString("mysql.host"), config.getString("mysql.port"), config.getString("mysql.name"), config.getString("mysql.user"), config.getString("mysql.pass"));
			//SQL.Connect();

			for (int i = 0; i < 5; i++) {
				MySQL.Connect();
				if (MySQL.isConnected()) {
					break;
				}
			}

			if (MySQL.isConnected()) {
				if (!MySQL.tableExists("point")) {
					MySQL.createTable("point", "uuid varchar(36) not null primary key, point int not null default 0");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		Bukkit.getPluginManager().registerEvents(new Prepare(), this);
		Bukkit.getPluginManager().registerEvents(new EmeraldItem(), this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		Bukkit.getScheduler().runTaskTimer(this, new MainTask(arena), 0L, 20L);


		super.onEnable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (command.getName().equalsIgnoreCase("fmh")) {
			if (args.length > 0) {
				if (!(sender instanceof Player)) {
					return true;
				}

				Player p = (Player) sender;

				if (args[0].equalsIgnoreCase("lobby")) {
					Arena.getInstance().setLobby(p.getLocation());;
					p.sendMessage("§eLobby Set Success!");
				}
				if (args[0].equalsIgnoreCase("seeker")) {
					Arena.getInstance().setSeekerSpawn(p.getLocation());;
					p.sendMessage("§eSeekerSpawn Set Success!");
				}
				if (args[0].equalsIgnoreCase("spawn")) {
					Arena.getInstance().addSpawn(p.getLocation());
					p.sendMessage("§eAddSpawn Success!");
				}
				
				if (args[0].equalsIgnoreCase("setseeker")) {
					if (args.length > 1) {
						Player pl = Bukkit.getPlayer(args[1]);
						if (pl != null) {
							Farm farm = Arena.getInstance().Farms.get(pl.getUniqueId());
							if (farm == null) {
								return true;
							}
							farm.setSeeker(true);
						}
					}
					return true;
				}


				return true;

			}
			sender.sendMessage("FarmHunt is running!");
			return true;
		}

		return super.onCommand(sender, command, label, args);
	}

	public static void sendLobby(Player p) {
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF("lobby");
			p.sendPluginMessage(Game.Instance, "BungeeCord", out.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
