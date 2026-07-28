package com.example.minibilling.model.domain;

public record StatsResponse(long totalUsers, int success, int failed, int skipped) {}