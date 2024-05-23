package com.betclic

import com.betclic.exceptions.PlayerAlreadyExistsException
import com.betclic.exceptions.PlayerNotFoundException
import com.betclic.models.PlayerDTO
import com.betclic.plugins.configureRouting
import com.betclic.plugins.configureSerialization
import com.betclic.services.IPlayerService
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.CompletableDeferred
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.test.inject
import org.koin.test.junit5.AutoCloseKoinTest
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import kotlin.test.assertEquals

val testModule = module {
    single<IPlayerService> { mock(IPlayerService::class.java) }
}

fun Application.testModule() {
    configureSerialization()

    val playerService: IPlayerService by inject()
    configureRouting(playerService)

    configureTestKoin()
}

fun configureTestKoin() {
    stopKoin()
    startKoin {
        modules(testModule)
    }
}

fun <R> withTestApplication(test: suspend ApplicationTestBuilder.() -> R) = testApplication {
    environment { config = MapApplicationConfig(
        "ktor.dynamodb.endpoint" to "http://0.0.0.0:4566",
        "ktor.dynamodb.region" to "af-south-1")
    }
    application { testModule() }
    test()
}

class PlayerControllerTest : AutoCloseKoinTest() {

    private val playerServiceMock: IPlayerService by inject()

    @BeforeEach
    fun setup() {
        stopKoin()
        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun `test_POST_Add player_Already existing player should return 409`(): Unit = withTestApplication {
        given(playerServiceMock.addPlayer(anyString(), anyInt())).willThrow(PlayerAlreadyExistsException::class.java)

        client.post("/player") {
            contentType(ContentType.Application.Json)
            setBody("""{"pseudo":"Clyde", "score": 4}""")
        }.apply {
            assertEquals(HttpStatusCode.Conflict, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).addPlayer("Clyde", 4)
    }

    @Test
    fun `test_POST_Add player_Invalid player payload should return 400`(): Unit = withTestApplication {
        client.post("/player") {
            contentType(ContentType.Application.Json)
            setBody("""{"nickname":"Clyde"}""")
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
        Mockito.verify(playerServiceMock, Mockito.never()).addPlayer(anyString(), anyInt())
    }

    @Test
    fun `test_POST_Add player_Nominal case (pseudo and score) should return 201`(): Unit = withTestApplication {
        val deferred = CompletableDeferred<Unit>()
        deferred.complete(Unit)
        doAnswer { _ -> deferred }.`when`(playerServiceMock).addPlayer(anyString(), anyInt())

        client.post("/player") {
            contentType(ContentType.Application.Json)
            setBody("""{"pseudo":"Clyde", "score": 6}""")
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).addPlayer("Clyde",6)
    }

    @Test
    fun `test_POST_Add player_Nominal case (pseudo only) should return 201`(): Unit = withTestApplication {
        val deferred = CompletableDeferred<Unit>()
        deferred.complete(Unit)
        doAnswer { _ -> deferred }.`when`(playerServiceMock).addPlayer(anyString(), anyInt())

        client.post("/player") {
            contentType(ContentType.Application.Json)
            setBody("""{"pseudo":"Clyde"}""")
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).addPlayer("Clyde",0)
    }

    @Test
    fun `test_PUT_Update player_Unknown player should return 404`(): Unit = withTestApplication {
        given(playerServiceMock.updatePlayerScore(anyString(), anyInt())).willThrow(PlayerNotFoundException::class.java)

        client.put("/player") {
            contentType(ContentType.Application.Json)
            setBody("""{"pseudo":"Barrow", "score": 66}""")
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).updatePlayerScore("Barrow",66)
    }


    @Test
    fun `test_PUT_Update player_Nominal case should return 200`(): Unit = withTestApplication {
        val deferred = CompletableDeferred<Unit>()
        deferred.complete(Unit)
        doAnswer { _ -> deferred }.`when`(playerServiceMock).updatePlayerScore(anyString(), anyInt())

        client.put("/player") {
            contentType(ContentType.Application.Json)
            setBody("""{"pseudo":"Bonnie", "score": 24}""")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).updatePlayerScore("Bonnie",24)
    }


    @Test
    fun `test_GET_Get all players_Nominal case should return 200`(): Unit = withTestApplication {
        given(playerServiceMock.getAllSortedPlayers()).willReturn(listOf())

        client.get("/players").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).getAllSortedPlayers()
    }

    @Test
    fun `test_GET_Get player_Unknown Pseudo should return 404`(): Unit = withTestApplication {
        given(playerServiceMock.getPlayer(anyString())).willThrow(PlayerNotFoundException::class.java)

        client.get("/player/unknown_pseudo").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).getPlayer("unknown_pseudo")
    }

    @Test
    fun `test_GET player_Empty Pseudo should return 400`(): Unit = withTestApplication {
        client.get("/player/").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
        Mockito.verify(playerServiceMock, Mockito.never()).getPlayer(anyString())
    }

    @Test
    fun `test_GET_Get player_Nominal case should return 200`(): Unit = withTestApplication {
        val hamilton = PlayerDTO(pseudo = "hamilton", score = 12, rank = 3)
        given(playerServiceMock.getPlayer("hamilton")).willReturn(hamilton)

        client.get("/player/hamilton").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).getPlayer("hamilton")
    }

    @Test
    fun `test_DELETE_Delete all players_Nominal case should return 200`(): Unit = withTestApplication {
        val deferred = CompletableDeferred<Unit>()
        deferred.complete(Unit)
        doAnswer { _ -> deferred }.`when`(playerServiceMock).deleteAllPlayers()

        client.delete("/players").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        Mockito.verify(playerServiceMock, Mockito.times(1)).deleteAllPlayers()
    }

}
