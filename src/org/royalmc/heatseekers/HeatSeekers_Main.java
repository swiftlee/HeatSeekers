package org.royalmc.heatseekers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class HeatSeekers_Main extends JavaPlugin implements Listener
{

	public JavaPlugin plugin = this;

	private static HashMap<Player, Integer> arrowsShot = new HashMap<Player, Integer>();
	private ArrayList<Player> quickPlayerAdd = new ArrayList<Player>();

	public void onEnable()
	{
		loadConfiguration();
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void loadConfiguration() 
	{
		String explosiondamage = "presets.explosionDamage";
		String explosionradius = "presets.explosionRadius";
		String seekingRadius = "presets.seekingRadius";
		String bowName = "presets.bowName";
		String bowDurability = "presets.bowDurability";
		String bowloreline1 = "presets.loreLine1";
		String bowloreline2 = "presets.loreLine2";


		plugin.getConfig().addDefault(explosiondamage, 4.0D);
		plugin.getConfig().addDefault(explosionradius, 5.0D);
		plugin.getConfig().addDefault(seekingRadius, 64.0D);
		plugin.getConfig().addDefault(bowName, "&c&lHeatSeeker Bow");
		plugin.getConfig().addDefault(bowDurability, 3);
		plugin.getConfig().addDefault(bowloreline1, "&eShoot this anywhere to seek");
		plugin.getConfig().addDefault(bowloreline2, "&ea player within " + (int)plugin.getConfig().getDouble("presets.seekingRadius") + " blocks!");

		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
	}

	public void reloadConfiguration() {
		plugin.reloadConfig();
	}


	@EventHandler 
	public void onHit(ProjectileHitEvent e)
	{
		if (e.getEntity() instanceof Arrow)
		{
			if(quickPlayerAdd.contains((Player)e.getEntity().getShooter()) || arrowsShot.get((Player)e.getEntity().getShooter()) != null)
			{
				Location loc = e.getEntity().getLocation();
				World w = loc.getWorld();
				loc = new Location(w, loc.getX(), loc.getY() + 0.2, loc.getZ());
				loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 0);
				loc.getWorld().playSound(loc, Sound.EXPLODE, 10.0f, 0f);
				Double r = plugin.getConfig().getDouble("presets.explosionRadius");
				for(Entity mob : e.getEntity().getNearbyEntities(r, r, r))
				{
					if(mob instanceof Player)
					{
						Player p = (Player)mob;
						p.damage(plugin.getConfig().getDouble("presets.explosionDamage"));
					}
				}

				Player p = (Player)e.getEntity().getShooter();
				quickPlayerAdd.remove(p);
			}
		}
	}

	@EventHandler
	public void onArrowShoot(EntityShootBowEvent e)
	{
		if(e.getBow().getItemMeta() != null && e.getBow().getItemMeta().getDisplayName() != null)
		{
			if(e.getBow().getItemMeta().getDisplayName().equals(formatString(plugin.getConfig().getString("presets.bowName"))))
			{
				if (((e.getEntity() instanceof LivingEntity)) && ((e.getProjectile() instanceof Arrow)))
				{

					if(!arrowsShot.containsKey((Player)e.getEntity()))
					{
						if(arrowsShot.get((Player)e.getEntity()) != null)
						{
							arrowsShot.put((Player)e.getEntity(), (arrowsShot.get((Player)e.getEntity()).intValue() + 1));
						}
						else
							arrowsShot.put((Player)e.getEntity(), 1);
					}
					else
					{
						arrowsShot.put((Player)e.getEntity(), (arrowsShot.get((Player)e.getEntity()).intValue() + 1));
					}

					if(arrowsShot.get((Player)e.getEntity()).intValue() < plugin.getConfig().getInt("presets.bowDurability") && arrowsShot.containsKey((Player)e.getEntity()))
					{

						LivingEntity player = e.getEntity();

						double minAngle = 6.283185307179586D;
						Entity minEntity = null;

						for (Entity entity : player.getNearbyEntities(plugin.getConfig().getDouble("presets.seekingRadius"), plugin.getConfig().getDouble("presets.seekingRadius"), 
								plugin.getConfig().getDouble("presets.seekingRadius"))) 
						{
							if (((entity instanceof Player)) && (!entity.isDead()))
							{
								Vector toTarget = entity.getLocation().toVector().clone().subtract(player.getLocation().toVector());
								double angle = e.getProjectile().getVelocity().angle(toTarget);

								if (angle < minAngle)
								{
									minAngle = angle;
									minEntity = entity;
								}
							}
						}

						if (minEntity != null) 
						{
							new SeekingTask((Arrow)e.getProjectile(), (LivingEntity)minEntity, this);
						}
					}
					else if(arrowsShot.get((Player)e.getEntity()).intValue() == plugin.getConfig().getInt("presets.bowDurability"))
					{


						LivingEntity player = e.getEntity();

						double minAngle = 6.283185307179586D;
						Entity minEntity = null;

						for (Entity entity : player.getNearbyEntities(plugin.getConfig().getDouble("presets.seekingRadius"), plugin.getConfig().getDouble("presets.seekingRadius"), 
								plugin.getConfig().getDouble("presets.seekingRadius"))) 
						{
							if (((entity instanceof Player)) && (!entity.isDead()))
							{
								Vector toTarget = entity.getLocation().toVector().clone().subtract(player.getLocation().toVector());
								double angle = e.getProjectile().getVelocity().angle(toTarget);

								if (angle < minAngle)
								{
									minAngle = angle;
									minEntity = entity;
								}
							}
						}

						if (minEntity != null) 
						{
							new SeekingTask((Arrow)e.getProjectile(), (LivingEntity)minEntity, this);
						}


						Player p = (Player)e.getEntity();

						if(p.getItemInHand().getItemMeta().getDisplayName().equals(formatString(plugin.getConfig().getString("presets.bowName"))))
						{
							quickPlayerAdd.add(p);
							((Player)e.getEntity()).getInventory().remove(((Player) e.getEntity()).getItemInHand());
							arrowsShot.remove((Player)e.getEntity());
						}
					}
				}
			}
		}
	}

	public static String formatString(String textToFormat) 
	{

		return textToFormat = ChatColor.translateAlternateColorCodes('&', textToFormat);

	}

	public List<String> heatSeekerLore(ItemMeta meta)
	{

		List<String> lore = new ArrayList<String>();

		lore.add(formatString(plugin.getConfig().getString("presets.loreLine1")));
		lore.add(formatString(plugin.getConfig().getString("presets.loreLine2")));

		return lore;
	}


	public ItemStack heatSeeker(int amt)
	{
		ItemStack seeker = new ItemStack(Material.BOW, amt);
		ItemMeta meta = seeker.getItemMeta();

		meta.setDisplayName(formatString(plugin.getConfig().getString("presets.bowName")));
		meta.setLore(heatSeekerLore(meta));
		seeker.setItemMeta(meta);
		seeker.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);


		return seeker;
	}

	public static void msg(CommandSender sender, String message) 
	{

		sender.sendMessage(formatString(message));

	}


	@SuppressWarnings("unused")
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player target = null;

		if(sender.isOp())
		{
			if(cmd.getName().equalsIgnoreCase("reloadhsconfig"))
			{

				if(plugin.getConfig() == null)
					loadConfiguration();

				plugin.reloadConfig();
				msg(sender, "&aSuccessfully reloaded HeatSeekers config.");
				return true;
			}
			if(cmd.getName().equalsIgnoreCase("heatseeker"))
			{
				if(sender instanceof Player)
				{

					if(args.length == 2)
					{

						target = Bukkit.getServer().getPlayer(args[0]);
						int count = 0;

						try
						{							
							count = Integer.parseInt(args[1]);
						}
						catch(NumberFormatException e)
						{

						}

						for(int i = 0; i < target.getInventory().getSize(); i++)
						{
							if (target != null) 
								target.getInventory().addItem(heatSeeker(count));
							if(target == null)
								msg(sender, "&cPlayer is offline.");

							break;

						}
					}


					else
					{
						msg(sender, "&a/hs <player> <amount>");
					}

					return true;
				}
			}

		}
		else
		{
			msg(sender, formatString("&4Only players may use this command!"));
		}

		return true;
	}
}
