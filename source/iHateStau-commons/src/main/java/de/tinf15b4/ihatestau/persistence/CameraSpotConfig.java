package de.tinf15b4.ihatestau.persistence;

import java.io.Serializable;
import java.util.Set;

public class CameraSpotConfig implements Serializable {

	private static final long serialVersionUID = 4553595639380768409L;

	private String id;
	private String cameraNameFront;
	private String cameraNameBack;
	private String name;
	private String maskFront;
	private String maskBack;
	private Set<String> lastAlternatives;
	private double cameraLat;
	private double cameraLon;
	private String street;
	private String sisterId;

	public CameraSpotConfig() {
	}

	public CameraSpotConfig(String id, String cameraNameFront, String cameraNameBack, String name, String maskFront,
			String maskBack, double cameraLat, double cameraLon, String street, Set<String> lastAlternatives,
			String sisterId) {
		this.id = id;
		this.cameraNameFront = cameraNameFront;
		this.cameraNameBack = cameraNameBack;
		this.name = name;
		this.maskFront = maskFront;
		this.maskBack = maskBack;
		this.cameraLat = cameraLat;
		this.cameraLon = cameraLon;
		this.street = street;
		this.lastAlternatives = lastAlternatives;
		this.sisterId = sisterId;
	}

	public CameraSpotConfig(String id, Set<String> lastAlternatives, String sisterId){
		this(id, null, null, null, null,
				null, 0, 0, null, lastAlternatives, sisterId);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCameraNameFront() {
		return cameraNameFront;
	}

	public String getCameraNameBack() {
		return cameraNameBack;
	}

	public String getName() {
		return name;
	}

	public String getMaskFront() {
		return maskFront;
	}

	public String getMaskBack() {
		return maskBack;
	}

	public double getCameraLat() {
		return cameraLat;
	}

	public double getCameraLon() {
		return cameraLon;
	}

	public Set<String> getLastAlternatives() {
		return lastAlternatives;
	}

	public void setLastAlternatives(Set<String> lastAlternatives) {
		this.lastAlternatives = lastAlternatives;
	}

	public String getStreet() {
		return street;
	}

	public String getSisterId() {
		return sisterId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(cameraLat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cameraLon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((cameraNameBack == null) ? 0 : cameraNameBack.hashCode());
		result = prime * result + ((cameraNameFront == null) ? 0 : cameraNameFront.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastAlternatives == null) ? 0 : lastAlternatives.hashCode());
		result = prime * result + ((maskBack == null) ? 0 : maskBack.hashCode());
		result = prime * result + ((maskFront == null) ? 0 : maskFront.hashCode());
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
		CameraSpotConfig other = (CameraSpotConfig) obj;
		if (Double.doubleToLongBits(cameraLat) != Double.doubleToLongBits(other.cameraLat))
			return false;
		if (Double.doubleToLongBits(cameraLon) != Double.doubleToLongBits(other.cameraLon))
			return false;
		if (cameraNameBack == null) {
			if (other.cameraNameBack != null)
				return false;
		} else if (!cameraNameBack.equals(other.cameraNameBack))
			return false;
		if (cameraNameFront == null) {
			if (other.cameraNameFront != null)
				return false;
		} else if (!cameraNameFront.equals(other.cameraNameFront))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastAlternatives == null) {
			if (other.lastAlternatives != null)
				return false;
		} else if (!lastAlternatives.equals(other.lastAlternatives))
			return false;
		if (maskBack == null) {
			if (other.maskBack != null)
				return false;
		} else if (!maskBack.equals(other.maskBack))
			return false;
		if (maskFront == null) {
			if (other.maskFront != null)
				return false;
		} else if (!maskFront.equals(other.maskFront))
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
