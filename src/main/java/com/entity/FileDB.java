package com.entity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;



import lombok.Getter;
import lombok.NoArgsConstructor;





@Entity
@Table(name = "files")
@NoArgsConstructor
public class FileDB {

  
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Type(type = "org.hibernate.type.BinaryType")
	@Column(name = "data")
	private byte[] data;
	@Column(name = "user_person_id")
	private Integer personId;
  
	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer personId) {
		this.personId = personId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	

	






}
