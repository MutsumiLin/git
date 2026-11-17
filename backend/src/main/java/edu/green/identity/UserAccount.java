package edu.green.identity;

public record UserAccount(long id, String username, String passwordHash, String role, Long merchantId, boolean enabled) { }
