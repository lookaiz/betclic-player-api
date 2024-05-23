package com.betclic.requests

import kotlinx.serialization.Serializable

@Serializable
data class PlayerRequest(
    val pseudo: String,
    val score: Int = 0
)