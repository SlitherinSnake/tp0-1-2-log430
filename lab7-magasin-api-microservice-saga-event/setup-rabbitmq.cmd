@echo off
REM RabbitMQ Configuration Script for Windows - Simplified Version
REM Configures exchanges, queues, and bindings for the event-driven choreographed saga architecture

echo ==============================================
echo     RABBITMQ EVENT-DRIVEN CONFIGURATION
echo ==============================================

echo [INFO] Waiting for RabbitMQ to be ready...
:wait_loop
docker exec rabbitmq rabbitmqctl await_startup >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] RabbitMQ is not ready yet, waiting...
    timeout /t 5 /nobreak >nul
    goto wait_loop
)

echo [SUCCESS] RabbitMQ is ready, starting configuration...

REM ==========================================
REM 1. CREATE EXCHANGES
REM ==========================================
echo.
echo [INFO] 1. Creating exchanges...

echo [INFO] Creating exchange: business.events (type: topic)
docker exec rabbitmq rabbitmqadmin declare exchange name=business.events type=topic durable=true

echo [INFO] Creating exchange: business.events.dlx (type: topic)
docker exec rabbitmq rabbitmqadmin declare exchange name=business.events.dlx type=topic durable=true

echo [INFO] Creating exchange: compensation.events (type: topic)
docker exec rabbitmq rabbitmqadmin declare exchange name=compensation.events type=topic durable=true

REM ==========================================
REM 2. CREATE QUEUES WITH DEAD LETTER HANDLING
REM ==========================================
echo.
echo [INFO] 2. Creating service queues with dead letter handling...

REM Transaction Service Queues
echo [INFO] Creating transaction service queues...
docker exec rabbitmq rabbitmqadmin declare queue name=transaction.events.queue durable=true arguments="{\"x-dead-letter-exchange\":\"business.events.dlx\",\"x-dead-letter-routing-key\":\"transaction.events.failed\",\"x-message-ttl\":3600000}"
docker exec rabbitmq rabbitmqadmin declare queue name=transaction.events.dlq durable=true

REM Payment Service Queues
echo [INFO] Creating payment service queues...
docker exec rabbitmq rabbitmqadmin declare queue name=payment.events.queue durable=true arguments="{\"x-dead-letter-exchange\":\"business.events.dlx\",\"x-dead-letter-routing-key\":\"payment.events.failed\",\"x-message-ttl\":3600000}"
docker exec rabbitmq rabbitmqadmin declare queue name=payment.events.dlq durable=true

REM Inventory Service Queues
echo [INFO] Creating inventory service queues...
docker exec rabbitmq rabbitmqadmin declare queue name=inventory.events.queue durable=true arguments="{\"x-dead-letter-exchange\":\"business.events.dlx\",\"x-dead-letter-routing-key\":\"inventory.events.failed\",\"x-message-ttl\":3600000}"
docker exec rabbitmq rabbitmqadmin declare queue name=inventory.events.dlq durable=true

REM Store Service Queues
echo [INFO] Creating store service queues...
docker exec rabbitmq rabbitmqadmin declare queue name=store.events.queue durable=true arguments="{\"x-dead-letter-exchange\":\"business.events.dlx\",\"x-dead-letter-routing-key\":\"store.events.failed\",\"x-message-ttl\":3600000}"
docker exec rabbitmq rabbitmqadmin declare queue name=store.events.dlq durable=true

REM Saga Orchestrator Queues
echo [INFO] Creating saga orchestrator queues...
docker exec rabbitmq rabbitmqadmin declare queue name=saga.events.queue durable=true arguments="{\"x-dead-letter-exchange\":\"business.events.dlx\",\"x-dead-letter-routing-key\":\"saga.events.failed\",\"x-message-ttl\":3600000}"
docker exec rabbitmq rabbitmqadmin declare queue name=saga.events.dlq durable=true

REM Audit Service Queues
echo [INFO] Creating audit service queues...
docker exec rabbitmq rabbitmqadmin declare queue name=audit.events.queue durable=true arguments="{\"x-dead-letter-exchange\":\"business.events.dlx\",\"x-dead-letter-routing-key\":\"audit.events.failed\",\"x-message-ttl\":3600000}"
docker exec rabbitmq rabbitmqadmin declare queue name=audit.events.dlq durable=true

REM Event Store Queues
echo [INFO] Creating event store queues...
docker exec rabbitmq rabbitmqadmin declare queue name=event-store.events.queue durable=true arguments="{\"x-dead-letter-exchange\":\"business.events.dlx\",\"x-dead-letter-routing-key\":\"event-store.events.failed\",\"x-message-ttl\":3600000}"
docker exec rabbitmq rabbitmqadmin declare queue name=event-store.events.dlq durable=true

REM ==========================================
REM 3. CREATE BINDINGS - TRANSACTION SERVICE
REM ==========================================
echo.
echo [INFO] 3. Creating bindings for transaction service...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=transaction.events.queue routing_key=payment.processed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=transaction.events.queue routing_key=payment.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=transaction.events.queue routing_key=payment.refunded
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=transaction.events.queue routing_key=inventory.unavailable

REM ==========================================
REM 4. CREATE BINDINGS - PAYMENT SERVICE
REM ==========================================
echo.
echo [INFO] 4. Creating bindings for payment service...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=payment.events.queue routing_key=transaction.created
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=payment.events.queue routing_key=transaction.cancelled
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=payment.events.queue routing_key=inventory.reserved

REM ==========================================
REM 5. CREATE BINDINGS - INVENTORY SERVICE
REM ==========================================
echo.
echo [INFO] 5. Creating bindings for inventory service...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=inventory.events.queue routing_key=payment.processed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=inventory.events.queue routing_key=payment.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=inventory.events.queue routing_key=store.order.fulfilled

REM ==========================================
REM 6. CREATE BINDINGS - STORE SERVICE
REM ==========================================
echo.
echo [INFO] 6. Creating bindings for store service...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=store.events.queue routing_key=inventory.reserved
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=store.events.queue routing_key=inventory.released
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=store.events.queue routing_key=payment.processed

REM ==========================================
REM 7. CREATE BINDINGS - SAGA ORCHESTRATOR
REM ==========================================
echo.
echo [INFO] 7. Creating bindings for saga orchestrator (receives all events)...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=saga.events.queue routing_key=transaction.#
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=saga.events.queue routing_key=payment.#
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=saga.events.queue routing_key=inventory.#
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=saga.events.queue routing_key=store.#

REM ==========================================
REM 8. CREATE BINDINGS - AUDIT SERVICE
REM ==========================================
echo.
echo [INFO] 8. Creating bindings for audit service (receives ALL events)...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=audit.events.queue routing_key=#

REM ==========================================
REM 9. CREATE BINDINGS - EVENT STORE
REM ==========================================
echo.
echo [INFO] 9. Creating bindings for event store (receives ALL events)...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events destination=event-store.events.queue routing_key=#

REM ==========================================
REM 10. CREATE DEAD LETTER BINDINGS
REM ==========================================
echo.
echo [INFO] 10. Creating dead letter bindings...
docker exec rabbitmq rabbitmqadmin declare binding source=business.events.dlx destination=transaction.events.dlq routing_key=transaction.events.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events.dlx destination=payment.events.dlq routing_key=payment.events.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events.dlx destination=inventory.events.dlq routing_key=inventory.events.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events.dlx destination=store.events.dlq routing_key=store.events.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events.dlx destination=saga.events.dlq routing_key=saga.events.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events.dlx destination=audit.events.dlq routing_key=audit.events.failed
docker exec rabbitmq rabbitmqadmin declare binding source=business.events.dlx destination=event-store.events.dlq routing_key=event-store.events.failed

REM ==========================================
REM CONFIGURATION SUMMARY
REM ==========================================
echo.
echo ==============================================
echo          CONFIGURATION COMPLETED
echo ==============================================
echo.
echo [SUCCESS] RabbitMQ configuration completed successfully!
echo.
echo Exchanges created:
echo   - business.events (topic)
echo   - business.events.dlx (topic) 
echo   - compensation.events (topic)
echo.
echo Service queues created:
echo   - transaction.events.queue + dlq
echo   - payment.events.queue + dlq
echo   - inventory.events.queue + dlq
echo   - store.events.queue + dlq
echo   - saga.events.queue + dlq
echo   - audit.events.queue + dlq
echo   - event-store.events.queue + dlq
echo.
echo Routing patterns configured:
echo   - Transaction Service: payment.processed, payment.failed, payment.refunded, inventory.unavailable
echo   - Payment Service: transaction.created, transaction.cancelled, inventory.reserved
echo   - Inventory Service: payment.processed, payment.failed, store.order.fulfilled
echo   - Store Service: inventory.reserved, inventory.released, payment.processed
echo   - Saga Orchestrator: ALL business events (transaction.#, payment.#, inventory.#, store.#)
echo   - Audit Service: ALL events (#)
echo   - Event Store: ALL events (#)
echo.
echo Access RabbitMQ Management UI at: http://localhost:15672
echo Username: guest / Password: guest
echo.
echo ==============================================
