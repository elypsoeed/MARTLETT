package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.QGitRepoPermissionEntity;
import com.elypsoeed.martlett.git.model.GitRepoPermission;
import com.elypsoeed.martlett.git.model.GitRepoPermissionSubjectType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.Collection;

public class GitRepoPermissionRepositoryImpl implements GitRepoPermissionRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public GitRepoPermissionRepositoryImpl(EntityManager entityManager) {
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	@Override
	public boolean hasPermission(
		Long repositoryId,
		GitRepoPermissionSubjectType subjectType,
		Long subjectId,
		Collection<GitRepoPermission> permissions
	) {
		QGitRepoPermissionEntity permission = QGitRepoPermissionEntity.gitRepoPermissionEntity;

		Integer result = queryFactory
			.selectOne()
			.from(permission)
			.where(
				permission.repo.id.eq(repositoryId),
				permission.subjectType.eq(subjectType),
				permission.subjectId.eq(subjectId),
				permission.permission.in(permissions)
			)
			.fetchFirst();

		return result != null;
	}
}
