package com.elypsoeed.martlett.common.testdata.model;

import com.elypsoeed.martlett.auth.model.Role;

import java.util.List;

public record TestUser(
        String username,
        String accessToken,
        String refreshToken,
        long accessExpiresIn,
        long refreshExpiresIn,
        List<Role> roles
) { }
