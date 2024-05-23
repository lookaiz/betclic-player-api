package com.betclic.services

import com.betclic.models.PlayerDTO

interface IPlayerService {

    suspend fun addPlayer(pseudo: String, score: Int)

    suspend fun updatePlayerScore(pseudo: String, newScore: Int)

    suspend fun getPlayer(pseudo: String): PlayerDTO

    suspend fun getAllSortedPlayers(): List<PlayerDTO>

    suspend fun deleteAllPlayers()
}
