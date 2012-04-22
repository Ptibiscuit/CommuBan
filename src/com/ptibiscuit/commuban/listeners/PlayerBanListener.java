package com.ptibiscuit.commuban.listeners;

import java.net.MalformedURLException;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.ptibiscuit.commuban.Ban;
import com.ptibiscuit.commuban.CommuBan;
import com.ptibiscuit.commuban.DataBan;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerBanListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLogin(PlayerLoginEvent e) {
		CommuBan commuBan = CommuBan.getInstance();
		try {
			if (!commuBan.getDataBan().getMysql().checkConnection())
			{
				return;
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		commuBan.getDataBan().setActivated(e.getPlayer().getName(), System.currentTimeMillis());
		
		Ban b = commuBan.getDataBan().isBanned(e.getPlayer().getName(), System.currentTimeMillis());
		if (b != null)
		{
			if (commuBan.getPermissionHandler().has(e.getPlayer(), "god", true))
				return;
			
			// Il est belle est bien banni !
			e.setResult(Result.KICK_OTHER);
			if (!b.isDefinitive())
			{
				e.setKickMessage(commuBan.getSentence("have_been_banned")
						.replace("+time", DataBan.getStringByTimeStamp(((b.getDuration() + b.getDate_activation()) - System.currentTimeMillis())/1000))
						.replace("+reason", b.getReason()));
			}
			else
			{
				e.setKickMessage(commuBan.getSentence("have_been_defbanned").replace("+reason", b.getReason()));
			}
		}
		
	}
	
}
