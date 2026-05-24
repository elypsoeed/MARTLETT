package com.elypsoeed.martlett.git.entity;

import com.elypsoeed.martlett.git.model.GitRepoPermissionSubjectType;
import com.elypsoeed.martlett.git.model.GitRepoRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "repository_roles")
public class GitRepoRoleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "repository_id", nullable = false)
	private GitRepoEntity repo;

	@Enumerated(EnumType.STRING)
	@Column(name = "subject_type", nullable = false)
	private GitRepoPermissionSubjectType subjectType;

	@Column(name = "subject_id", nullable = false)
	private Long subjectId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private GitRepoRole role;
}
