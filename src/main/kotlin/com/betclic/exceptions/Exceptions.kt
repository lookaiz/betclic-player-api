package com.betclic.exceptions

class PlayerAlreadyExistsException(message: String) : RuntimeException(message)

class PlayerNotFoundException(message: String) : RuntimeException(message)

class DataAccessException(message: String) : RuntimeException(message)
