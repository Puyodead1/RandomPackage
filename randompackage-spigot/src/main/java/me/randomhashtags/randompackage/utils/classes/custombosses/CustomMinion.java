package me.randomhashtags.randompackage.utils.classes.custombosses;

import java.util.List;

public class CustomMinion {
	public String type, name;
	public List<String> attributes;
	public CustomMinion(String type, String name, List<String> attributes) {
		this.type = type;
		this.name = name;
		this.attributes = attributes;
	}
}