package com.elypsoeed.martlett.common.entity;

import com.elypsoeed.martlett.common.model.Sex;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "usrs")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(name = "registration_timestamp")
	private Instant registrationTimestamp;

	@Enumerated(EnumType.STRING)
	private Sex sex;

	private Integer age;

	private String city;

	@Column(name = "tg_contact")
	private String tgContact;

	@Column(name = "email_contact")
	private String emailContact;

	@Column(name = "place_of_work")
	private String placeOfWork;

	@Column(name = "avatar_content_type")
	private String avatarContentType;

	@Column(name = "avatar_data")
	private byte[] avatarData;
}
