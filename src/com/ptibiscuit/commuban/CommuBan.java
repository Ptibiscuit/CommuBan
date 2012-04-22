package com.ptibiscuit.commuban;

import com.ptibiscuit.commuban.listeners.PlayerBanListener;
import com.ptibiscuit.framework.JavaPluginEnhancer;
import com.ptibiscuit.framework.PermissionHelper;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommuBan extends JavaPluginEnhancer {
	
	private static CommuBan instance;
	private PlayerBanListener pbl = new PlayerBanListener();
	private DataBan dataBan = new DataBan();
	
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onEnable() {
		this.setup(ChatColor.GREEN + "[CommuBan]", "commuban", true);

		this.getMyLogger().startFrame();
		this.getMyLogger().addInFrame("CommuBan by Ptibiscuit");
		this.getMyLogger().addCompleteLineInFrame();
		
		CommuBan.instance = this;
		if (PermissionHelper.setupPermissions(this.getServer()))
		{
			this.getMyLogger().addInFrame("Using Permissions !");
		}
		else
		{
			this.getMyLogger().addInFrame("Using default OP Permissions.");
		}
		this.getMyLogger().displayFrame(false);
		if (!this.dataBan.init(Logger.getLogger("Minecraft"), "[CommuBan]", this.getConfig().getString("bdd.host"), this.getConfig().getString("bdd.database"), this.getConfig().getString("bdd.login"), this.getConfig().getString("bdd.password")))
		{
			return;
		}
		this.getServer().getPluginManager().registerEvents(pbl, this);
	}

	public boolean canBanHim(String banner, String banned)
	{
		Player pBanner = this.getServer().getPlayer(banner);
		//Player pBanned = this.getServer().getPlayer(banned);
		if (!this.permissionHandler.has(pBanner, "ban", true))
			return false;
		return true;
	}
	
	public static CommuBan getInstance() {
		return instance;
	}

	public DataBan getDataBan() {
		return dataBan;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		try
		{
			if (!dataBan.getMysql().checkConnection())
			{
				this.sendPreMessage(sender, "connection_off");
				return true;
			}
			
			if (!(sender instanceof Player))
			{
				return true;
			}
			Player p = (Player) sender;
			if (label.equalsIgnoreCase("cban"))
			{
				if (!this.canBanHim(sender.getName(), args[0]))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				String[] dataTime = args[1].split("\\.");
				long timeAdd = 0;
				try
				{
					for (int i = 0;i < dataTime.length;i++)
					{
						if (i < this.dataBan.getConvers().size())
							timeAdd += Integer.parseInt(dataTime[i]) * this.dataBan.getConvers().get(i);
					}
				}
				catch (NumberFormatException e)
				{
					this.sendPreMessage(p, "put_number");
				}
				String reason = "";
				for (int i = 2;i < args.length;i++)
				{
					reason += args[i] + " ";
				}
				reason = reason.trim() + " - " + sender.getName();
				OfflinePlayer pFoc = this.getServer().getOfflinePlayer(args[0]);
				if (pFoc != null)
				{
					// On a notre temps, on va bannir notre joueur. Seulement timeAdd est en secondes, et pas en millisecondes !
					this.dataBan.banPlayer(pFoc.getName(), reason.trim(), System.currentTimeMillis(), timeAdd * 1000);

					this.displayMessage(this.getSentence("has_been_banned").replace("+pseudo", pFoc.getName()).replace("+time", (DataBan.getStringByTimeStamp(timeAdd)).trim()).replace("+reason", reason.trim()));
					this.myLog.log(this.getSentence("has_been_banned").replace("+pseudo", pFoc.getName()).replace("+time", (DataBan.getStringByTimeStamp(timeAdd)).trim()).replace("+reason", reason.trim()));

					if (pFoc != null && pFoc.isOnline() && !this.permissionHandler.has(pFoc.getPlayer(), "god", true))
					{
						this.dataBan.setActivated(pFoc.getName(), System.currentTimeMillis());
						pFoc.getPlayer().kickPlayer(this.getSentence("have_been_banned")
								.replace("+time", (DataBan.getStringByTimeStamp(timeAdd)).trim())
								.replace("+reason", reason.trim()));
						this.sendPreMessage(sender, "player_kick");
					}
				}
				else
				{
					this.sendPreMessage(sender, "no_player");
				}
			}
			else if (label.equalsIgnoreCase("cpardon"))
			{
				if (!this.permissionHandler.has(sender, "pardon", true))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				OfflinePlayer pFoc = this.getServer().getOfflinePlayer(args[0]);
				if (pFoc != null)
				{
					this.dataBan.debanPlayerFromDate(pFoc.getName(), System.currentTimeMillis());
					this.sendMessage(sender, this.getSentence("deban_player").replace("+pseudo", pFoc.getName()));
					this.myLog.log(this.getSentence("deban_player").replace("+pseudo", pFoc.getName()));
				}
				else
				{
					this.sendPreMessage(sender, "no_player");
				}
			}
			else if (label.equalsIgnoreCase("ckickserver"))
			{
				if (!this.permissionHandler.has(sender, "kick", true))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				
				Player pFoc = this.getServer().getPlayer(args[0]);
				if (pFoc != null)
				{
					if (this.permissionHandler.has(pFoc, "god", true))
					{
						this.sendPreMessage(sender, "cant_command");
						return true;
					}
					
					String reason = "";
					for (int i = 1;i < args.length;i++)
					{
						reason += args[i] + " ";
					}
					reason = reason + " - " + p.getName();
					this.dataBan.kickPlayer(args[0], reason.trim(), System.currentTimeMillis());
					this.displayMessage(this.getSentence("have_been_kicked").replace("+pseudo", pFoc.getName()).replace("+reason", reason));
					this.myLog.log(this.getSentence("have_been_kicked").replace("+pseudo", pFoc.getName()).replace("+reason", reason));
					
					pFoc.kickPlayer(reason);
				}
				else
				{
					this.sendPreMessage(sender, "no_player");
				}
			}
			else if (label.equalsIgnoreCase("dcban"))
			{
				if (!this.canBanHim(sender.getName(), args[0]))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				String reason = "";
				for (int i = 1;i < args.length;i++)
				{
					reason += args[i] + " ";
				}
				reason = reason.trim() + " - " + sender.getName();
				OfflinePlayer pFoc = this.getServer().getOfflinePlayer(args[0]);
				if (pFoc != null)
				{
					// On a notre temps, on va bannir notre joueur. Seulement timeAdd est en secondes, et pas en millisecondes !
					this.dataBan.defbanPlayer(args[0], reason.trim(), System.currentTimeMillis());

					this.displayMessage(this.getSentence("has_been_defbanned").replace("+pseudo", pFoc.getName()));
					this.myLog.log(this.getSentence("has_been_defbanned").replace("+pseudo", pFoc.getName()));
					if (pFoc.isOnline() && !this.permissionHandler.has(pFoc.getPlayer(), "god", true))
					{
						pFoc.getPlayer().kickPlayer(this.getSentence("have_been_defbanned").replace("+reason", reason.trim()));
						this.dataBan.setActivated(args[0], System.currentTimeMillis());
						this.sendPreMessage(sender, "player_kick");
					}
				}
				else
				{
					this.sendPreMessage(sender, "no_player");
				}
			}
			else if (label.equalsIgnoreCase("ccheck"))
			{
				if (!this.permissionHandler.has(sender, "check", true))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				OfflinePlayer pFoc = this.getServer().getOfflinePlayer(args[0]);
				if (pFoc != null)
				{
					List<Ban> bans = this.dataBan.getPlayerBans(args[0]);
					if (!bans.isEmpty())
					{
						this.sendPreMessage(sender, "list_bans");
						for (Ban b : bans)
						{
							// On produit une date lisible:
							SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
							String dateFormated = ChatColor.GOLD + "(" + form.format(new Date(b.getDate_begin())) + ")" + ChatColor.WHITE;

							// On regarde si il est deleté:
							String deleteSymbol = (b.isDeleted()) ? ChatColor.DARK_GREEN + "[Revoque]" + ChatColor.WHITE: "";
							if (b.getDuration() > 0)
								this.sendMessage(sender, deleteSymbol + dateFormated + dataBan.getStringByTimeStamp(b.getDuration()/1000) + ": " + b.getReason());
							else if (b.getDuration() == 0)
							{
								if (b.isDefinitive())
									this.sendMessage(sender, deleteSymbol + dateFormated + this.getSentence("forever") + ": " + b.getReason());
								else
									this.sendMessage(sender, deleteSymbol + dateFormated + this.getSentence("kick") + ": " + b.getReason());
							}
						}
					}
					else
					{
						this.sendPreMessage(sender, "no_past");
					}
				}
				else
				{
					this.sendPreMessage(sender, "no_player");
				}
				
			}
			else if (label.equalsIgnoreCase("csynchro"))
			{
				if (!this.permissionHandler.has(sender, "synchro", true))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				int c = 0;
				for (OfflinePlayer offP : this.getServer().getBannedPlayers())
				{
					Ban b = this.dataBan.isBanned(offP.getName(), System.currentTimeMillis());
					if (b == null)
					{
						// They are banned, but they are not banned in the plugin.
						this.dataBan.defbanPlayer(offP.getName(), "No reason - " + p.getName(), System.currentTimeMillis());
						c++;
					}
				}
				this.sendMessage(sender, this.getSentence("synchro_succ").replace("+count", new Integer(c).toString()));
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			this.sendPreMessage(sender, "bad_use");
			return false;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public void displayMessage(String m)
	{
		for (Player p : this.getServer().getOnlinePlayers())
		{
			if (this.permissionHandler.has(p, "sawer", false))
				this.sendMessage(p, m);
		}
	}
	
	@Override
	public void onConfigurationDefault(FileConfiguration c) {
		c.set("bdd.host", "");
		c.set("bdd.login", "");
		c.set("bdd.password", "");
		c.set("bdd.database", "");
	}

	@Override
	public void onLangDefault(Properties p) {
		p.setProperty("cant_command", "Vous ne pouvez pas utiliser cette commande.");
		p.setProperty("connection_off", "La connection avec la Bdd est coupée !");
		p.setProperty("has_been_banned", "+pseudo" + ChatColor.RED + " a été banni pour +time car " + ChatColor.YELLOW + "+reason" + ChatColor.WHITE + ".");
		p.setProperty("has_been_defbanned", "+pseudo" + ChatColor.RED + " a été banni définitivement.");
		p.setProperty("deban_player", "+pseudo a été débanni.");
		p.setProperty("have_been_banned", "Vous avez été banni pour +time, raison: +reason");
		p.setProperty("have_been_kicked", "+pseudo" + ChatColor.RED + " a été kické car" + ChatColor.YELLOW + " +reason." + ChatColor.WHITE + "");
		p.setProperty("have_been_defbanned", "Vous avez été banni définitivement, raison: +reason");
		p.setProperty("player_kick", "Le joueur a été kické !");
		p.setProperty("no_player", "Ce joueur n'existe pas.");
		p.setProperty("bad_use", "Mauvaise utilisation de la commande.");
		p.setProperty("no_past", "Aucun ban n'a été fait sur ce joueur.");
		p.setProperty("synchro_succ", "Synchro terminée, ajout de +count bannis.");
		p.setProperty("list_bans", "Liste des bans sur ce joueur:");
		p.setProperty("put_number", "Utilise des nombres, débiles !");
		p.setProperty("cant_ban_him", "Vous ne pouvez pas ce grand type.");
		p.setProperty("forever", "Très longtemps ...");
		p.setProperty("kick", "Kick");
		
	}

}
