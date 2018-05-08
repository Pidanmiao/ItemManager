package org.com.itemmanager.Util;

public class Item {
	public String name;
	public String mac;
	public String location;

	@Override
	public String toString() {
		return "{ name="+name+", mac="+mac+", location="+location+" }";
	}
}
