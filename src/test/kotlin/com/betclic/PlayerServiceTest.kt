package com.betclic

import com.betclic.exceptions.PlayerNotFoundException
import com.betclic.models.Player
import com.betclic.models.PlayerDTO
import com.betclic.repositories.IPlayerRepository
import com.betclic.services.IPlayerService
import com.betclic.services.PlayerService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlayerServiceTest {

    private lateinit var playerRepositoryMock: IPlayerRepository
    private lateinit var playerService: IPlayerService

    private val player1 = Player("player1", 50)
    private val player2 = Player("player2", 100)
    private val player3 = Player("player3", 75)

    @BeforeEach
    fun setup() {
        playerRepositoryMock = mock(IPlayerRepository::class.java)
        playerService = PlayerService(playerRepositoryMock)

        runBlocking {
            given(playerRepositoryMock.findAll()).willReturn(listOf(player1, player2, player3))
        }
    }

    private fun comparePlayers(player: Player, returnedPlayer: PlayerDTO) {
        assertEquals(player.pseudo, returnedPlayer.pseudo)
        assertEquals(player.score, returnedPlayer.score)
    }

    @Test
    fun `test getAllSortedPlayers`() = runBlocking {
        val expectedSortedPlayers = listOf(
            PlayerDTO("player2", 100, 1),
            PlayerDTO("player3", 75, 2),
            PlayerDTO("player1", 50, 3)
        )

        val sortedPlayers = playerService.getAllSortedPlayers()
        assertEquals(expectedSortedPlayers, sortedPlayers)
    }

    @Test
    fun `test getPlayer`() = runBlocking {
        comparePlayers(player1, playerService.getPlayer("player1"))
        comparePlayers(player2, playerService.getPlayer("player2"))
        comparePlayers(player3, playerService.getPlayer("player3"))
    }

    @Test
    fun `test getPlayer-Unknown player`(): Unit = runBlocking {
        assertFailsWith<PlayerNotFoundException> {
            playerService.getPlayer("player4")
        }
    }

    @Test
    fun `test getPlayer-No players`(): Unit = runBlocking {
        given(playerRepositoryMock.findAll()).willReturn(listOf())

        assertFailsWith<PlayerNotFoundException> {
            playerService.getPlayer("player1")
        }
    }

}
