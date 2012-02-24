package com.ptibiscuit.commuban.listeners;

import java.net.MalformedURLException;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.ptibiscuit.commuban.Ban;
import com.ptibiscuit.commuban.CommuBan;
import com.ptibiscuit.commuban.DataBan;
import com.ptibiscuit.framework.PermissionHelper;

public class PlayerBanListener extends PlayerListener {

	public void onPlayerLogin(PlayerLoginEvent e) {
		try {
			if (!CommuBan.getInstance().getDataBan().getMysql().checkConnection())
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
		
		CommuBan.getInstance().getDataBan().setActivated(e.getPlayer().getName(), System.currentTimeMillis());
		
		Ban b = CommuBan.getInstance().getDataBan().isBanned(e.getPlayer().getName(), System.currentTimeMillis());
		if (b != null)
		{
			if (PermissionHelper.has(e.getPlayer(), CommuBan.getInstance().getPrefixPermissions() + ".god", true))
				return;
			
			// Il est belle est bien banni !
			e.setResult(Result.KICK_OTHER);
			if (!b.isDefinitive())
			{
				e.setKickMessage(CommuBan.getInstance().getSentence("have_been_banned")
						.replace("+time", DataBan.getStringByTimeStamp(((b.getDuration() + b.getDate_activation()) - System.currentTimeMillis())/1000))
						.replace("+reason", b.getReason()));
			}
			else
			{
				e.setKickMessage(CommuBan.getInstance().getSentence("have_been_defbanned").replace("+reason", b.getReason()));
			}
		}
		
	}
	
}
