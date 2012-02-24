package com.ptibiscuit.commuban;

import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import com.ptibiscuit.framework.mysql.mysqlCore;

public class DataBan {
	private mysqlCore mysql;
	private LinkedHashMap<Integer, Integer> convers = new LinkedHashMap<Integer, Integer>();
	
	public static int[] getFormatByTimeStamp(long tp)
	{
		int[] format = new int[3];
		format[0] = (int) (tp/(60*60*24));
		long restByDay = tp%(60*60*24);
		format[1] = (int) (restByDay/(60*60));
		long restByHour = restByDay%(60*60);
		format[2] = (int) (restByHour/60);
		return format;
	}
	
	public static String getStringByTimeStamp(long tp)
	{
		return getStringByFormat(getFormatByTimeStamp(tp));
	}
	
	public static String getStringByFormat(int[] tps)
	{
		String message = "";
		if (tps[0] != 0)
			message += tps[0] + " jours ";
		if (tps[1] != 0)
			message += tps[1] + " heures ";
		if (tps[2] != 0)
			message += tps[2] + " minutes ";
		if (message.isEmpty())
			message = "1 minutes ";
		
		return message;
		
	}
	
	public boolean init(Logger l, String prefix, String host, String database, String login, String password)
	{
		convers.put(0, 60);
		convers.put(1, 3600);
		convers.put(2, 86400);
		
		try {
			mysql = new mysqlCore(l, prefix, host, database, login, password);
			mysql.initialize();
			if (mysql.checkConnection())
			{
				return true;
			}
			else
			{
				return false;
			}
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
		return false;
	}
	
	public boolean setActivated(String banned, long actualTime)
	{
		try {
			mysql.updateQuery("UPDATE ba_bans SET BA_activated = 1, BA_date_activation = " + actualTime + " WHERE BA_banned = \"" + banned + "\" AND BA_activated = 0;");
			return true;
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
		return false;
	}
	
	public boolean defbanPlayer(String banned, String reason, long date_begin)
	{
		try {
			mysql.insertQuery("INSERT INTO ba_bans(BA_banned, BA_reason, BA_date_begin, BA_definitive) VALUES(\"" + banned + "\", \"" + reason + "\", " + date_begin + ", 1)");
			return true;
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
		return false;
	}
	
	public List<Ban> getPlayerBans(String banned)
	{
		try {
			ResultSet rs = mysql.sqlQuery("SELECT * FROM ba_bans WHERE BA_banned = \"" + banned + "\";");
			if (rs != null)
			{
				ArrayList<Ban> bans = new ArrayList<Ban>();
				while (rs.next())
					bans.add(new Ban(rs.getInt("BA_id"), rs.getString("BA_banned"), rs.getLong("BA_date_begin"), rs.getLong("BA_duration"), rs.getLong("BA_date_activation"), rs.getInt("BA_activated"), rs.getInt("BA_definitive"), rs.getString("BA_reason"), rs.getInt("BA_deleted")));
				return bans; 
			}
			return null;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean banPlayer(String banned, String reason, long date_begin, long duration)
	{
		try {
			mysql.insertQuery("INSERT INTO ba_bans(BA_banned, BA_reason, BA_date_begin, BA_duration) VALUES(\"" + banned + "\", \"" + reason + "\", " + date_begin + ", " + duration + ");");
			return true;
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
		
		return false;
	}
	
	public boolean kickPlayer(String banned, String reason, long date_begin)
	{
		try {
			mysql.insertQuery("INSERT INTO ba_bans(BA_banned, BA_reason, BA_date_begin, BA_duration) VALUES(\"" + banned + "\", \"" + reason + "\", " + date_begin + ", 0);");
			return true;
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
		
		return false;
	}
	
	public boolean debanPlayerFromDate(String player, long date)
	{
		try {
			mysql.deleteQuery("UPDATE ba_bans SET BA_deleted = 1 WHERE BA_banned = \"" + player + "\" AND (BA_activated = 0 OR((BA_activated = 1 AND ((BA_definitive = 1) OR (BA_definitive = 0 AND BA_duration + BA_date_activation > " + date + ")))));");
			return true;
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
		
		return false;
	}
	
	public Ban isBanned(String name, long actualTime)
	{
		try {
			
			ResultSet rs = mysql.sqlQuery("SELECT * FROM ba_bans WHERE BA_deleted = 0 AND BA_banned = \"" + name + "\" AND ((BA_definitive = 1) OR (BA_definitive = 0 AND BA_activated = 1 AND (BA_date_activation + BA_duration > " + actualTime + ")));");
			if (rs.next())
			{
				return new Ban(rs.getInt("BA_id"), rs.getString("BA_banned"), rs.getLong("BA_date_begin"), rs.getLong("BA_duration"), rs.getLong("BA_date_activation"), rs.getInt("BA_activated"), rs.getInt("BA_definitive"), rs.getString("BA_reason"), rs.getInt("BA_deleted"));
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public com.ptibiscuit.framework.mysql.mysqlCore getMysql() {
		return mysql;
	}

	public HashMap<Integer, Integer> getConvers() {
		return convers;
	}
	
	/* ba_bans
	 * - BA_id
	 * - BA_banned
	 * - BA_reason
	 * - BA_date_begin
	 * - BA_date_activation
	 * - BA_duration
	 * - BA_activated
	 * - BA_definitive
	 */
}
