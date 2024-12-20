package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *     AlertLevel level;
 *     AlertDescription description;
 * } Alert;
 * </pre>
 * 
 * @author ZhangXi 2024年12月20日
 */
public class Alert extends TLSPlaintext {

	private AlertLevel level;
	private AlertDescription description;

	public Alert() {
	}

	public Alert(AlertLevel level, AlertDescription description) {
		this.description = description;
		this.level = level;
	}

	@Override
	public ContentType contentType() {
		return ContentType.ALERT;
	}

	public AlertDescription getDescription() {
		return description;
	}

	public void setDescription(AlertDescription value) {
		description = value;
	}

	public void setDescription(int value) {
		description = AlertDescription.code(value);
	}

	public AlertLevel getLevel() {
		return level;
	}

	public void setLevel(AlertLevel value) {
		level = value;
	}

	public void setLevel(int value) {
		level = AlertLevel.code(value);
	}

	@Override
	public String toString() {
		if (getLevel() != null) {
			if (getDescription() != null) {
				return getLevel().name() + ':' + getDescription().name();
			} else {
				return getLevel().name();
			}
		} else {
			if (getDescription() != null) {
				return getDescription().name();
			} else {
				return "";
			}
		}
	}
}