package de.tinf15b4.ihatestau.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
public class CameraSpotTrafficStateEntity implements Serializable {

	private static final long serialVersionUID = -4731098768449408835L;

	@EmbeddedId
	private TrafficStateId id;

	@Column
	private float jamProbability;

	@Transient
	private float jamProbabilitySmooth;

	@Transient
	private byte[] imageFront;

	@Transient
	private byte[] imageBack;

	/**
	 * <b>THIS IS NOT MEANT TO BE USED</b>
	 */
	public CameraSpotTrafficStateEntity() {
		// needed for hibernate
	}

	public CameraSpotTrafficStateEntity(TrafficStateId id, float jamProbability, float jamProbabilitySmooth, byte[] imageFront, byte[] imageBack) {
		super();
		this.id = id;
		this.jamProbability = jamProbability;
		this.imageFront = imageFront;
		this.imageBack = imageBack;
		this.jamProbabilitySmooth = jamProbabilitySmooth;
	}

	public byte[] getImageBack() {
		return imageBack;
	}

	public byte[] getImageFront() {
		return imageFront;
	}

	public float getJamProbability() {
		return jamProbability;
	}

	public float getJamProbabilitySmooth() {
		return jamProbabilitySmooth;
	}

	public TrafficStateId getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + Float.floatToIntBits(jamProbability);
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
		CameraSpotTrafficStateEntity other = (CameraSpotTrafficStateEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (Float.floatToIntBits(jamProbability) != Float.floatToIntBits(other.jamProbability))
			return false;
		return true;
	}

}
