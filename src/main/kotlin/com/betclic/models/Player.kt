package com.betclic.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(val pseudo: String, val score: Int)

@Serializable
data class PlayerDTO(val pseudo: String, val score: Int, val rank: Int)