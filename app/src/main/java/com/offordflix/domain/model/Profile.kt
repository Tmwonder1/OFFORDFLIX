package com.offordflix.domain.model

/**
 * Domain model representing a user profile.
 */
data class Profile(
    val id: String,
    val name: String,
    val avatarId: Int,
    val isKidsProfile: Boolean,
    val createdAt: Long,
    val lastUsed: Long
)


