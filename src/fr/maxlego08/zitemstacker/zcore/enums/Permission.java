package fr.maxlego08.zitemstacker.zcore.enums;

public enum Permission {

	;

	private String permission;

	private Permission() {
		this.permission = this.name().toLowerCase().replace("_", ".");
	}

	public String getPermission() {
		return permission;
	}

}
