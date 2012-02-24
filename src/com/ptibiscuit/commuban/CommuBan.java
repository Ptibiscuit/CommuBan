package com.ptibiscuit.commuban;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.util.config.Configuration;

import com.ptibiscuit.commuban.listeners.PlayerBanListener;
import com.ptibiscuit.framework.JavaPluginEnhancer;
import com.ptibiscuit.framework.PermissionHelper;
import com.ptibiscuit.framework.mysql.mysqlCore;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		this.setup("CommuBan", ChatColor.GREEN + "[CommuBan]", "commuban", true);

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
		if (!this.dataBan.init(Logger.getLogger("Minecraft"), "[CommuBan]", this.getConfiguration().getString("bdd.host"), this.getConfiguration().getString("bdd.database"), this.getConfiguration().getString("bdd.login"), this.getConfiguration().getString("bdd.password")))
		{
			return;
		}
		
		this.getServer().getPluginManager().registerEvent(Type.PLAYER_LOGIN, pbl, Priority.Lowest, this);
	}

	public boolean canBanHim(String banner, String banned)
	{
		Player pBanner = this.getServer().getPlayer(banner);
		//Player pBanned = this.getServer().getPlayer(banned);
		if (!PermissionHelper.has(pBanner, this.prefixPermissions + ".ban", true))
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
				// On a notre temps, on va bannir notre joueur. Seulement timeAdd est en secondes, et pas en millisecondes !
				this.dataBan.banPlayer(args[0], reason.trim(), System.currentTimeMillis(), timeAdd * 1000);
				
				this.displayMessage(this.getSentence("has_been_banned").replace("+pseudo", args[0]).replace("+time", (DataBan.getStringByTimeStamp(timeAdd)).trim()).replace("+reason", reason.trim()));
				this.myLog.log(this.getSentence("has_been_banned").replace("+pseudo", args[0]).replace("+time", (DataBan.getStringByTimeStamp(timeAdd)).trim()).replace("+reason", reason.trim()));
				Player pFoc = this.getServer().getPlayerExact(args[0]);
				if (pFoc != null && !PermissionHelper.has(pFoc, this.prefixPermissions + ".god", true))
				{
					this.dataBan.setActivated(args[0], System.currentTimeMillis());
					pFoc.kickPlayer(this.getSentence("have_been_banned")
							.replace("+time", (DataBan.getStringByTimeStamp(timeAdd)).trim())
							.replace("+reason", reason.trim()));
					this.sendPreMessage(sender, "player_kick");
				}
			}
			else if (label.equalsIgnoreCase("cpardon"))
			{
				if (!PermissionHelper.has(sender, this.prefixPermissions + ".pardon", true))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				
				this.dataBan.debanPlayerFromDate(args[0], System.currentTimeMillis());
				this.sendMessage(sender, this.getSentence("deban_player").replace("+pseudo", args[0]));
				this.myLog.log(this.getSentence("deban_player").replace("+pseudo", args[0]));
			}
			else if (label.equalsIgnoreCase("ckickserver"))
			{
				if (!PermissionHelper.has(sender, this.prefixPermissions + ".kick", true))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				
				Player pFoc = this.getServer().getPlayer(args[0]);
				if (pFoc != null)
				{
					if (PermissionHelper.has(pFoc, this.prefixPermissions + ".god", true))
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
				// On a notre temps, on va bannir notre joueur. Seulement timeAdd est en secondes, et pas en millisecondes !
				this.dataBan.defbanPlayer(args[0], reason.trim(), System.currentTimeMillis());
				
				this.displayMessage(this.getSentence("has_been_defbanned").replace("+pseudo", args[0]));
				this.myLog.log(this.getSentence("has_been_defbanned").replace("+pseudo", args[0]));
				Player pFoc = this.getServer().getPlayerExact(args[0]);
				if (pFoc != null && !PermissionHelper.has(pFoc, this.prefixPermissions + ".god", true))
				{
					pFoc.kickPlayer(this.getSentence("have_been_defbanned").replace("+reason", reason.trim()));
					this.dataBan.setActivated(args[0], System.currentTimeMillis());
					this.sendPreMessage(sender, "player_kick");
				}
			}
			else if (label.equalsIgnoreCase("ccheck"))
			{
				if (!PermissionHelper.has(sender, this.prefixPermissions + ".check", true))
				{
					this.sendPreMessage(sender, "cant_command");
					return true;
				}
				
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
			else if (label.equalsIgnoreCase("csynchro"))
			{
				if (!PermissionHelper.has(sender, this.prefixPermissions + ".synchro", true))
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
			if (PermissionHelper.has(p, this.prefixPermissions + ".sawer", false))
				this.sendMessage(p, m);
		}
	}
	
	@Override
	public void onConfigurationDefault(Configuration c) {
		c.setProperty("bdd.host", "");
		c.setProperty("bdd.login", "");
		c.setProperty("bdd.password", "");
		c.setProperty("bdd.database", "");
	}

	@Override
	public void onLangDefault(Properties p) {
		p.setProperty("cant_command", "Vous ne pouvez pas utiliser cette commande.");
		p.setProperty("connection_off", "La connection avec la Bdd est coupée !");
		p.setProperty("has_been_banned", "+pseudo" + ChatColor.RED + " a été banni pour +time car " + ChatColor.YELLOW + "+reason" + ChatColor.WHITE + ".");
		p.setProperty("has_been_defbanned", "+pseudo" + ChatColor.RED + " a été banni d�finitivement.");
		p.setProperty("deban_player", "+pseudo a été débanni.");
		p.setProperty("have_been_banned", "Vous avez été banni pour +time, raison: +reason");
		p.setProperty("have_been_kicked", "+pseudo" + ChatColor.RED + " a été kické car" + ChatColor.YELLOW + " +reason." + ChatColor.WHITE + "");
		p.setProperty("have_been_defbanned", "Vous avez été banni d�finitivement, raison: +reason");
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
