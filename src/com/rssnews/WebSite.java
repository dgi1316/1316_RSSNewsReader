package com.rssnews;

public class WebSite {

	Integer _id;
	String _title;
	String _link;
	String _rss_link;
	String _description;

	public WebSite() {

	}

	public WebSite(String title, String link, String rss_link,
			String description) {
		this._title = title;
		this._link = link;
		this._rss_link = rss_link;
		this._description = description;
	}

	public void setId(Integer id) {
		this._id = id;
	}

	public void setTitle(String title) {
		this._title = title;
	}

	public void setLink(String link) {
		this._link = link;
	}

	public void setRSSLink(String rss_link) {
		this._rss_link = rss_link;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	public Integer getId() {
		return this._id;
	}

	public String getTitle() {
		return this._title;
	}

	public String getLink() {
		return this._link;
	}

	public String getRSSLink() {
		return this._rss_link;
	}

	public String getDescription() {
		return this._description;
	}

}
