package com.joyzl.network.tls;

public class Alert extends Record {

	private AlertLevel level;
	private AlertDescription description;

	public AlertDescription getDescription() {
		return description;
	}

	public void setDescription(AlertDescription value) {
		description = value;
	}

	public AlertLevel getLevel() {
		return level;
	}

	public void setLevel(AlertLevel value) {
		level = value;
	}
}