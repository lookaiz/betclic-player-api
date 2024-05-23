# Betclic Player Tournament API

Technical recruitment test

## Description
This Kotlin-based application exposes a REST API for managing the ranking of players in a tournament.

This API exposes classic CRUD operations and allows to :

- Add a new player identified by their nickname and original score
- Update a player's score
- Retrieve a player's data (nickname, score and ranking in the tournament)
- Return players sorted by their score (highest score first)
- Delete all players

## Prerequisites
- Java 17+
- Gradle
- Docker
- Docker Compose
- Terraform
- (Optional) NoSQL Workbench for DynamoDB

## Technical stack
This application is based upon following technologies :

- [Kotlin](https://kotlinlang.org/) as main language
- [Ktor](https://ktor.io/) as application server
- [Koin](https://insert-koin.io/) for dependency injection
- [AWS DynamoDB](https://aws.amazon.com/dynamodb/) as storage layer
- [LocalStack](https://www.localstack.cloud/) as a drop-in replacement for AWS (local dev)
- [Docker Compose](https://docs.docker.com/compose/) to run LocalStack in a Docker container (local dev)
- [Terraform](https://www.terraform.io/) for provisioning DynamoDB table (local dev)
- (Optional) [NoSQL Workbench for DynamoDB](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/workbench.html) to browse DynamoDB tables


## How to launch the application
1. Clone current git repository
2. Launch `LocalStack` container instance (local dev only). Wait for the instance to start up completely before continuing
```
> cd ./docker
> docker-compose up -d
```
3. Create DynamoDB table `Player` using Terraform and file `dynamodb_player_table.tf`
```
> cd ./infra
> terraform init
> terraform apply --auto-approve
```
4. Edit `startServer.sh` file and fill in variable `DYNAMODB_ENDPOINT` with the appropriate value
```
DYNAMODB_ENDPOINT=http://localhost:4566
```
5. Build then launch Ktor server packaged as FatJar.
```
./startServer.sh
```

The server is available on port `8081` by default.
This port can be changed in the `application.conf` configuration file


## API endpoints description
The API provides the following resources

* *POST /player* : create a new player
* *PUT /player* : update player score
* *GET /players* : retrieve all players sorted by score (highest score first)
* *GET /player/${pseudo}* : get player data
* *DELETE /players* : remove all players

### Endpoint details

| POST | /player |    
|------|---------|

Create a new player. Original score of player is optional (0 by default)

The payload should be a JSON object with the following properties:
* `pseudo`: (required) a string containing the player's nickame (must be unique)
* `score`: (optional) an integer containing the player's score.

*Body examples :*
```json
{ "pseudo":"John", "score":10 }
```
```json
{ "pseudo":"Jane" }
```
HTTP code
* `201 : Created`

* `409 : Conflict`. Player already exists 

*Example* : `curl -i -X POST http://localhost:8081/player -H "Content-Type: application/json" -d '{"pseudo":"PlayerOne"}'`


| PUT | /player |    
|-----|---------|

Update player score

The payload should be a JSON object with the following properties:
* `pseudo`: (required) a string containing the player's nickame
* `score`: (required) a string containing the player's score.

*Body example :*
```json
{
  "pseudo": "John",
  "score":25
}
```
HTTP code
* `200 : OK`
* `404 : Not Found`. Player does not exist

*Example* : `curl -i -X PUT http://localhost:8081/player -H "Content-Type: application/json" -d '{"pseudo":"PlayerOne", "score":100}'`

| GET | /players |    
|-----|----------|

Get all players sorted by score (highest score first)

*Example* :
```json
[
  { "pseudo": "John", "score": 25, "rank": 1 },
  { "pseudo": "Jane", "score": 10, "rank": 2 },
  { "pseudo": "Mike", "score": 8, "rank": 3 }
]
```
HTTP code
* `200 : OK`

*Example* : `curl -i -X GET "http://localhost:8081/players"`

| GET | /player/{pseudo} |    
|-----|------------------|

Get player data

*Example* :
```json
{ "pseudo": "John", "score": 25, "rank": 1 }
```
HTTP code
* `200 : OK`
* `400 : Bad Request`. ${pseudo} is empty
* `404 : Not Found`. Player does not exist

*Example* : `curl -i -X GET "http://localhost:8081/player/John"`

| DELETE | /players |    
|--------|----------|

Remove all players

HTTP code
* `200 : OK`

*Example* : `curl -i -X DELETE "http://localhost:8081/players"`

### Improvements
* Add authentication layer to acces API
* Add integration tests (end-to-end)
* Use Docker for DynamoDB for local environment
* Add a configuration mechanism per environment (local, integration, pre-prod, prod)
* Improve error handling (add exceptions)
* Redirect logs to Elasticsearch instance for monitoring purpose
* Improve ranking calculation mechanism. Can be time-consuming depending on the number of players.
* Improve 'Delete all players' implementation. Players are removed one by one (use batch or delete then recreate table ?)
* Evaluate impact of default billing mode (pay per request) 
