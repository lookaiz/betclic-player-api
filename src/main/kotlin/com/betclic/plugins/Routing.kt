package com.betclic.plugins

import com.betclic.exceptions.DataAccessException
import com.betclic.exceptions.PlayerAlreadyExistsException
import com.betclic.exceptions.PlayerNotFoundException
import com.betclic.requests.PlayerRequest
import com.betclic.services.IPlayerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(playerService: IPlayerService) {
    routing {
        // Add a new player identified by their pseudo and optional score
        post("/player") {
            try {
                val playerRequest = call.receive<PlayerRequest>()
                playerService.addPlayer(playerRequest.pseudo, playerRequest.score)
                logger().info("New player with pseudo '${playerRequest.pseudo}' added")
                call.respond(HttpStatusCode.Created)
            }
            catch (paee: PlayerAlreadyExistsException) {
                logger().info("Cannot add player : '${paee.message}'")
                call.respond(HttpStatusCode.Conflict)
            }
            catch (dae: DataAccessException) {
                logger().info("Cannot add player : '${dae.message}'")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        // Update a player's score
        put("/player") {
            try {
                val playerRequest = call.receive<PlayerRequest>()
                playerService.updatePlayerScore(playerRequest.pseudo, playerRequest.score)
                logger().info("Update score of player '${playerRequest.pseudo}' to '${playerRequest.score}'")
                call.respond(HttpStatusCode.OK)
            }
            catch(pnfe: PlayerNotFoundException) {
                logger().info("Cannot update player score : '${pnfe.message}'")
                call.respond(HttpStatusCode.NotFound)
            }
            catch (dae: DataAccessException) {
                logger().info("Cannot update player score : '${dae.message}'")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        // Return players sorted by their score (highest score first)
        get("/players") {
            val sortedPlayers = playerService.getAllSortedPlayers()
            call.respond(HttpStatusCode.OK, sortedPlayers)
        }

        // Retrieve a player's data (nickname, score and ranking in the tournament)
        get("/player/{pseudo?}") {
            try {
                val pseudo = call.parameters["pseudo"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val player = playerService.getPlayer(pseudo)
                call.respond(HttpStatusCode.OK, player)
            }
            catch (pnfe: PlayerNotFoundException) {
                logger().info("Cannot retrieve player informations : ${pnfe.message}")
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Delete all players
        delete("/players") {
            try {
                playerService.deleteAllPlayers()
                logger().info("All players have been removed from the database")
                call.respond(HttpStatusCode.OK)
            }
            catch (dae: DataAccessException) {
                logger().info("Cannot deleta all players : '${dae.message}'")
                call.respond(HttpStatusCode.InternalServerError)
            }

        }

    }
}
