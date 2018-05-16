package de.tinf15b4.ihatestau.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class TrafficStateId implements Serializable {

	private static final long serialVersionUID = -6114315749131670802L;

	@Column
	private String spotId;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	/**
	 * <b>THIS IS NOT MEANT TO BE USED</b>
	 */
	public TrafficStateId() {
		// needed for hibernate
	}

	public TrafficStateId(String spotConfig, Date timestamp) {
		this.spotId = spotConfig;
		this.timestamp = timestamp;
	}

	public String getSpotId() {
		return spotId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spotId == null) ? 0 : spotId.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
		TrafficStateId other = (TrafficStateId) obj;
		if (spotId == null) {
			if (other.spotId != null)
				return false;
		} else if (!spotId.equals(other.spotId))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

}