package de.tinf15b4.ihatestau.persistence;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class CameraSpotConfigEntity implements Serializable {

	private static final long serialVersionUID = -3767148071241003930L;

	@Id
	private String id;

	@Column
	private String cameraNameFront;

	@Column
	private String cameraNameBack;

	@Column
	private String name;

	@Column
	private String maskFront;

	@Column
	private String maskBack;

	@Column
	private double cameraLat;

	@Column
	private double cameraLon;

	@ManyToOne
	private StreetEntity street;

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<ExitSpotConfigEntity> lastAlternativeExits;

	@OneToOne
	private CameraSpotConfigEntity sister;

	/**
	 * <b>THIS IS NOT MEANT TO BE USED</b>
	 */
	public CameraSpotConfigEntity() {
		// needed for hibernate
	}

	public String getId() {
		return id;
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

	public StreetEntity getStreet() {
		return street;
	}

	public Set<ExitSpotConfigEntity> getLastAlternativeExits() {
		return lastAlternativeExits;
	}

	public CameraSpotConfigEntity getSister() {
		return sister;
	}

	public CameraSpotConfig toDTO() {
		return new CameraSpotConfig(id, cameraNameFront, cameraNameBack, name, maskFront, maskBack, cameraLat,
				cameraLon, street.getName(),
				lastAlternativeExits.stream().map(ExitSpotConfigEntity::getId).collect(Collectors.toSet()),
				sister != null ? sister.getId() : null);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCameraNameFront(String cameraNameFront) {
		this.cameraNameFront = cameraNameFront;
	}

	public void setCameraNameBack(String cameraNameBack) {
		this.cameraNameBack = cameraNameBack;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMaskFront(String maskFront) {
		this.maskFront = maskFront;
	}

	public void setMaskBack(String maskBack) {
		this.maskBack = maskBack;
	}

	public void setCameraLat(double cameraLat) {
		this.cameraLat = cameraLat;
	}

	public void setCameraLon(double cameraLon) {
		this.cameraLon = cameraLon;
	}

	public void setStreet(StreetEntity street) {
		this.street = street;
	}

	public void setLastAlternativeExits(Set<ExitSpotConfigEntity> lastAlternativeExits) {
		this.lastAlternativeExits = lastAlternativeExits;
	}

	public void setSister(CameraSpotConfigEntity sister) {
		this.sister = sister;
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
		result = prime * result + ((lastAlternativeExits == null) ? 0 : lastAlternativeExits.hashCode());
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
		CameraSpotConfigEntity other = (CameraSpotConfigEntity) obj;
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
		if (lastAlternativeExits == null) {
			if (other.lastAlternativeExits != null)
				return false;
		} else if (!lastAlternativeExits.equals(other.lastAlternativeExits))
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
