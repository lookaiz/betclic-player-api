package com.betclic.repositories

import com.betclic.exceptions.DataAccessException
import com.betclic.exceptions.PlayerAlreadyExistsException
import com.betclic.exceptions.PlayerNotFoundException
import com.betclic.models.Player
import com.betclic.plugins.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest

private const val PLAYER_TABLE_NAME = "player"
private const val ATTR_PSEUDO = "pseudo"
private const val ATTR_SCORE = "score"

class PlayerRepository(private val dynamoDbClient: DynamoDbClient) : IPlayerRepository {

    override suspend fun findAll(): List<Player> = withContext(Dispatchers.IO) {
        val scanRequest = ScanRequest.builder().tableName(PLAYER_TABLE_NAME).build()
        val players = dynamoDbClient.scan(scanRequest).items()
            .map { item ->
                Player(
                    pseudo = item[ATTR_PSEUDO]!!.s(),
                    score = item[ATTR_SCORE]!!.n().toInt(),
                )
            }
        logger().info("${players.size} players have been found in the database")
        players
    }

    override suspend fun save(pseudo: String, score: Int): Unit = withContext(Dispatchers.IO) {
        val player = mutableMapOf(
            ATTR_PSEUDO to AttributeValue.builder().s(pseudo).build(),
            ATTR_SCORE to AttributeValue.builder().n(score.toString()).build()
        )
        val request = PutItemRequest.builder()
            .tableName(PLAYER_TABLE_NAME)
            .item(player)
            .conditionExpression("attribute_not_exists($ATTR_PSEUDO)")
            .build()

        try {
            dynamoDbClient.putItem(request)
            logger().info("The player '$pseudo' with a score of '$score' has been created in the database")
        }
        catch (e: ConditionalCheckFailedException) {
            throw PlayerAlreadyExistsException("The player '$pseudo' already exists")
        }
        catch (dbe: DynamoDbException) {
            throw DataAccessException("Player cannot be added to database. Cause : ${dbe.message}")
        }
    }

    override suspend fun updateScore(pseudo: String, newScore: Int): Unit = withContext(Dispatchers.IO) {
        val key = mapOf(ATTR_PSEUDO to AttributeValue.builder().s(pseudo).build())
        val updateExpression = "SET $ATTR_SCORE = :newScore"
        val expressionAttributeValues = mapOf(
            ":newScore" to AttributeValue.builder().n(newScore.toString()).build()
        )
        val request = UpdateItemRequest.builder()
            .tableName(PLAYER_TABLE_NAME)
            .key(key)
            .updateExpression(updateExpression)
            .expressionAttributeValues(expressionAttributeValues)
            .conditionExpression("attribute_exists($ATTR_PSEUDO)")
            .build()

        try {
            dynamoDbClient.updateItem(request)
            logger().info("The score of the player '$pseudo' has been updated to '$newScore'")
        }
        catch (e: ConditionalCheckFailedException) {
            throw PlayerNotFoundException("Player '$pseudo' does not exist")
        }
        catch (dbe: DynamoDbException) {
            throw DataAccessException("The new score '$newScore' of the player '$pseudo' cannot be updated. Cause : ${dbe.message}")
        }
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        try {
            val scanRequest = ScanRequest.builder().tableName(PLAYER_TABLE_NAME).build()
            dynamoDbClient.scan(scanRequest).items()
                .forEach { item ->
                    val pseudo = item[ATTR_PSEUDO]!!.s()
                    val deleteItemRequest = DeleteItemRequest.builder()
                        .tableName(PLAYER_TABLE_NAME)
                        .key(mapOf(ATTR_PSEUDO to AttributeValue.builder().s(pseudo).build()))
                        .build()
                    dynamoDbClient.deleteItem(deleteItemRequest)
                }
            logger().info("All players have been removed from the database")
        }
        catch (dbe: DynamoDbException) {
            throw DataAccessException("Players cannot be deleted. Cause : ${dbe.message}")
        }
    }

}
