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
import java.sql.PreparedStatement;
import java.util.logging.Level;

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
			PreparedStatement req = mysql.prepareStatement("UPDATE ba_bans SET BA_activated = 1, BA_date_activation = ? WHERE BA_banned = ? AND BA_activated = 0;");
			req.setLong(1, actualTime);
			req.setString(2, banned);
			req.execute();
			return true;
		} catch (SQLException ex) {
			Logger.getLogger(DataBan.class.getName()).log(Level.SEVERE, null, ex);
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
			PreparedStatement req = mysql.prepareStatement("INSERT INTO ba_bans(BA_banned, BA_reason, BA_date_begin, BA_definitive) VALUES(?, ?, ?, 1)");
			req.setString(1, banned);
			req.setString(2, reason);
			req.setLong(3, date_begin);
			req.execute();
			return true;
		} catch (SQLException ex) {
			Logger.getLogger(DataBan.class.getName()).log(Level.SEVERE, null, ex);
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
			PreparedStatement req = mysql.prepareStatement("SELECT * FROM ba_bans WHERE BA_banned = ?;");
			req.setString(1, banned);
			ResultSet rs = req.executeQuery();
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
			PreparedStatement req = mysql.prepareStatement("INSERT INTO ba_bans(BA_banned, BA_reason, BA_date_begin, BA_duration) VALUES(?, ?, ?, ?);");
			req.setString(1, banned);
			req.setString(2, reason);
			req.setLong(3, date_begin);
			req.setLong(4, duration);
			req.execute();
			return true;
		} catch (SQLException ex) {
			Logger.getLogger(DataBan.class.getName()).log(Level.SEVERE, null, ex);
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
			PreparedStatement req = mysql.prepareStatement("INSERT INTO ba_bans(BA_banned, BA_reason, BA_date_begin, BA_duration) VALUES(?, ?, ?, 0);");
			req.setString(1, banned);
			req.setString(2, reason);
			req.setLong(3, date_begin);
			req.execute();
			return true;
		} catch (SQLException ex) {
			Logger.getLogger(DataBan.class.getName()).log(Level.SEVERE, null, ex);
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
			PreparedStatement req = mysql.prepareStatement("UPDATE ba_bans SET BA_deleted = 1 WHERE BA_banned = ? AND (BA_activated = 0 OR((BA_activated = 1 AND ((BA_definitive = 1) OR (BA_definitive = 0 AND BA_duration + BA_date_activation > ?)))));");
			req.setString(1, player);
			req.setLong(2, date);
			req.execute();
			return true;
		} catch (SQLException ex) {
			Logger.getLogger(DataBan.class.getName()).log(Level.SEVERE, null, ex);
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
			PreparedStatement req = mysql.prepareStatement("SELECT * FROM ba_bans WHERE BA_deleted = 0 AND BA_banned = ? AND ((BA_definitive = 1) OR (BA_definitive = 0 AND BA_activated = 1 AND (BA_date_activation + BA_duration > ?)));");
			req.setString(1, name);
			req.setLong(2, actualTime);
			ResultSet rs = req.executeQuery();
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
