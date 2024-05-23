package com.betclic.services

import com.betclic.exceptions.PlayerNotFoundException
import com.betclic.models.PlayerDTO
import com.betclic.repositories.IPlayerRepository

class PlayerService(private val playerRepository: IPlayerRepository) : IPlayerService {

    override suspend fun addPlayer(pseudo: String, score: Int) = playerRepository.save(pseudo, score)

    override suspend fun updatePlayerScore(pseudo: String, newScore: Int) = playerRepository.updateScore(pseudo, newScore)

    override suspend fun getAllSortedPlayers(): List<PlayerDTO> {
        return playerRepository.findAll()
            .sortedByDescending { it.score }
            .mapIndexed { index, player -> PlayerDTO(pseudo = player.pseudo, score = player.score, index + 1) }
    }

    override suspend fun getPlayer(pseudo: String): PlayerDTO {
        return this.getAllSortedPlayers().find { it.pseudo == pseudo }
            ?: throw PlayerNotFoundException("Player with pseudo '$pseudo' does not exist")
    }

    override suspend fun deleteAllPlayers() = playerRepository.deleteAll()

}