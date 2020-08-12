package net.devras.farmhunt;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.devras.farmhunt.util.ParticleAPI;
import net.devras.farmhunt.util.ParticleAPI.EnumParticle;
import net.devras.farmhunt.util.ParticleAPI.Particle;

public class Farm {
	private Player player;
	private DisguiseType type;
	private boolean isSeeker;

	public Farm(Player p, DisguiseType type, boolean isSeeker) {
		this.player = p;
		this.type = type;
		this.isSeeker = isSeeker;
	}

	public DisguiseType getAnimal() {
		return type;
	}

	public void setAnimal(Entity entity) {
		EntityType eType = entity.getType();

		if (eType.equals(EntityType.OCELOT)) {
			type = DisguiseType.OCELOT;
		}

		if (eType.equals(EntityType.WOLF)) {
			type = DisguiseType.WOLF;
		}

		if (eType.equals(EntityType.CHICKEN)) {
			type = DisguiseType.CHICKEN;
		}

		if (eType.equals(EntityType.COW)) {
			type = DisguiseType.COW;
		}

		if (eType.equals(EntityType.PIG)) {
			type = DisguiseType.PIG;
		}

		if (eType.equals(EntityType.SHEEP)) {
			type = DisguiseType.SHEEP;
		}

		if (eType.equals(EntityType.HORSE)) {
			type = DisguiseType.HORSE;
		}

	}

	public boolean Guise() {
		if (!Arena.getInstance().isInGame) {
			if (DisguiseAPI.isDisguised(player)) {
				DisguiseAPI.undisguiseToAll(player);
			}
			return true;
		}
		if (isSeeker) {
			if (DisguiseAPI.isDisguised(player)) {
				DisguiseAPI.undisguiseToAll(player);
			}
			return isSeeker;
		}

		if (DisguiseAPI.isDisguised(player)) {
			Disguise guise = DisguiseAPI.getDisguise(player);
			if (guise.getType().equals(type)) {
				return true;
			}
		}

		for (int x = 0; x < 10; x++) {
			Particle part = new ParticleAPI.Particle(EnumParticle.SMOKE_LARGE, player.getLocation(), x, x, x, 1, 3, false);
			part.sendParticle();
		}

		MobDisguise guise = new MobDisguise(type);
		guise.setEntity(player);
		return guise.startDisguise();
	}

	public void armor() {
		PlayerInventory inventory = player.getInventory();
		// reset
		inventory.clear();
		inventory.setArmorContents(null);

		// set
		inventory.setItem(0, new ItemStack(Material.DIAMOND_SWORD));
		inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
		inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
	}

	public boolean isSeeker() {
		return isSeeker;
	}

	public void setSeeker(boolean isSeeker) {
		this.isSeeker = isSeeker;
	}

}
