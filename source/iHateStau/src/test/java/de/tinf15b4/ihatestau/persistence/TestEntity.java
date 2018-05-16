package de.tinf15b4.ihatestau.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TestEntity {
	@Id
	@GeneratedValue
	private long id;

	@Column
	private String string;

	public TestEntity() {
	}

	public TestEntity(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

}