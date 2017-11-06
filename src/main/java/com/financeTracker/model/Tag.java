package com.financeTracker.model;

public class Tag {
	private long tagId;
	private String name;
	private long userId;
	
	public Tag(String name, long userId) {
		this.name = name;
		this.userId = userId;
	}
	
	public Tag(long tagId, String name, long userId) {
		this.tagId = tagId;
		this.name = name;
		this.userId = userId;
	}
	
	public long getTagId() {
		return tagId;
	}
	
	public void setTagId(long tagId) {
		this.tagId = tagId;
	}
	
	public String getName() {
		return name;
	}
	
	public long getUserId() {
		return userId;
	}
	
	@Override
	public String toString() {
		return "Tag [tagId=" + tagId + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
