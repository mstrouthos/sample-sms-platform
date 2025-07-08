SMS Platform Example

- sms-service - API responsible for sending and listing SMS messages (http://localhost:8081)
- processor-service - Consumes the messages from RabbitMQ and randomly replies with a success or failure
- RabbitMQ as a message broker
- PostgreSQL database
- OpenAPI/Swagger UI documentation available at http://localhost:8081/swagger-ui/

The application is dockerized 
You can run it simply by running the command:

docker-compose up --build -d

Test Scenarios:
- Send message with empty text
- Send message with invalid phone number
- Check listing messages after queuing
- Checki message statuses updating after processed