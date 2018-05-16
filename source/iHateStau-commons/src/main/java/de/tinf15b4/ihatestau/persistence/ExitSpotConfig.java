package de.tinf15b4.ihatestau.persistence;

import java.io.Serializable;

public class ExitSpotConfig implements Serializable {

	private static final long serialVersionUID = -8152062754194621200L;

	private String id;
	private String name;
	private double exitLat;
	private double exitLon;
	private String street;

	/**
	 * <b>THIS IS NOT MEANT TO BE USED</b>
	 */
	public ExitSpotConfig() {
		// needed for hibernate
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getExitLat() {
		return exitLat;
	}

	public double getExitLon() {
		return exitLon;
	}

	public String getStreet() {
		return street;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExitLat(double exitLat) {
		this.exitLat = exitLat;
	}

	public void setExitLon(double exitLon) {
		this.exitLon = exitLon;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(exitLat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(exitLon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
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
		ExitSpotConfig other = (ExitSpotConfig) obj;
		if (Double.doubleToLongBits(exitLat) != Double.doubleToLongBits(other.exitLat))
			return false;
		if (Double.doubleToLongBits(exitLon) != Double.doubleToLongBits(other.exitLon))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		return true;
	}

}
