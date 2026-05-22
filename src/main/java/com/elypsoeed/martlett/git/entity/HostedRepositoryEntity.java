package com.elypsoeed.martlett.git.entity;

import com.elypsoeed.martlett.common.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "hosted_repositories")
public class HostedRepositoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_user_id", nullable = false)
	private UserEntity owner;

	@Column(nullable = false)
	private String name;

	@Column(name = "storage_relative_path", nullable = false, unique = true)
	private String storageRelativePath;

	@Column(name = "created_timestamp", nullable = false)
	private Instant createdTimestamp;
}
