package com.betclic.repositories

import com.betclic.models.Player

interface IPlayerRepository {

    suspend fun findAll(): List<Player>

    suspend fun save(pseudo: String, score: Int)

    suspend fun updateScore(pseudo: String, newScore: Int)

    suspend fun deleteAll()
}
