provider "aws" {
  alias                       = "localstack"
  region                      = "eu-west-3"
  access_key                  = "fake"
  secret_key                  = "fake"

  skip_credentials_validation = true
  skip_requesting_account_id  = true
  skip_region_validation      = true

  endpoints {
    dynamodb = "http://localstack:4566" # Localstack endpoint
  }
}

resource "aws_dynamodb_table" "player" {
  provider     = aws.localstack
  name         = "player"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pseudo"

  attribute {
    name = "pseudo"
    type = "S"
  }
}
