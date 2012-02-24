package com.ptibiscuit.commuban;

public class Ban {
	private int id;
	private long date_begin;
	private long date_activation;
	private int activated;
	private int definitive;
	private long duration;
	private String banned;
	private String reason;
	private int deleted;
	
	public Ban(int id, String banned, long date_begin,
			long duration, long date_activation, int activated, int definitive, String reason, int deleted) {
		super();
		this.id = id;
		this.date_begin = date_begin;
		this.date_activation = date_activation;
		this.activated = activated;
		this.duration = duration;
		this.definitive = definitive;
		this.banned = banned;
		this.reason = reason;
		this.deleted = deleted;
	}
	
	public long getDate_begin() {
		return date_begin;
	}
	public String getBanned() {
		return banned;
	}
	public String getReason() {
		return reason;
	}
	public int getId() {
		return id;
	}
	public long getDate_activation() {
		return date_activation;
	}
	public boolean isActivated() {
		return (activated == 0) ? false : true;
	}
	public long getDuration() {
		return duration;
	}

	public boolean isDefinitive() {
		return(definitive == 0) ? false : true;
	}
	
	public boolean isDeleted()
	{
		return (deleted == 0) ? false : true;
	}
}
