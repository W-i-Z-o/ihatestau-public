package de.tinf15b4.ihatestau.persistence;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {

	@Id
	private String username;

	@Column
	private byte[] password;

	@Column
	private byte[] salt;

	/**
	 * <b>THIS IS NOT MEANT TO BE USED</b>
	 */
	public User() {
		// needed for hibernate
	}

	public User(String username, byte[] password, byte[] salt) {
		this.username = username;
		this.password = password;
		this.salt = salt;
	}

	public String getUsername() {
		return username;
	}

	public byte[] getPassword() {
		return password;
	}

	public byte[] getSalt() {
		return salt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(password);
		result = prime * result + Arrays.hashCode(salt);
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		User other = (User) obj;
		if (!Arrays.equals(password, other.password))
			return false;
		if (!Arrays.equals(salt, other.salt))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
