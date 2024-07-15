package com.joyzl.network.ftp;

import java.time.LocalDateTime;

/**
 * FTP File
 * 
 * @author ZhangXi 2024年7月12日
 */
public class FTPFile {

	private LocalDateTime lastModified;
	private String name, owner, group, parent;
	private char[] permissions;
	private boolean directory;
	private int links;
	private long size;

	public String getName() {
		return name;
	}

	protected void setName(String value) {
		name = value;
	}

	public String getOwner() {
		return owner;
	}

	protected void setOwner(String value) {
		owner = value;
	}

	public String getGroup() {
		return group;
	}

	protected void setGroup(String value) {
		group = value;
	}

	public String getParent() {
		return parent;
	}

	protected void setParent(String value) {
		parent = value;
	}

	public boolean isDirectory() {
		return directory;
	}

	protected void setDirectory(boolean value) {
		directory = value;
	}

	public int getLinks() {
		return links;
	}

	protected void setLinks(int value) {
		links = value;
	}

	public long getSize() {
		return size;
	}

	protected void setSize(long value) {
		size = value;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	protected void setLastModified(LocalDateTime value) {
		lastModified = value;
	}

	public char[] getPermissions() {
		return permissions;
	}

	protected void setPermissions(char[] value) {
		permissions = value;
	}

	@Override
	public String toString() {
		if (isDirectory()) {
			return getName();
		}
		return getName() + ' ' + getSize();
	}
}