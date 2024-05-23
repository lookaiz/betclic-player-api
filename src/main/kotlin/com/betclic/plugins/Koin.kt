package com.betclic.plugins

import com.betclic.repositories.IPlayerRepository
import com.betclic.repositories.PlayerRepository
import com.betclic.services.IPlayerService
import com.betclic.services.PlayerService
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

fun Application.configureKoin() {
    startKoin {
        modules(dynamoDBModule, koinModule)
    }
}

val koinModule = module {
    single<IPlayerRepository> { PlayerRepository(get()) }
    single<IPlayerService> { PlayerService(get()) }
}

val dynamoDBModule = module {
    single {
        val config = ConfigFactory.load()
        val endpoint = config.getString("ktor.dynamodb.endpoint")
        val region = config.getString("ktor.dynamodb.region")

        DynamoDbClient.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("accessKey", "secretKey")
                )
            )
            .build()
    }
}