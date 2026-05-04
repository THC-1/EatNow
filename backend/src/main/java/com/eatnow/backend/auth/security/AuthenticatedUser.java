package com.eatnow.backend.auth.security;

import java.util.List;

public record AuthenticatedUser(Long userId, String username, List<String> roles) {
}
