CONTAINER ?= docker

.PHONY: build test smoke run clean reset-db up down

build:
	./gradlew build -x test

test:
	./gradlew test

smoke:
	./gradlew :app:test

run:
	./gradlew :app:bootRun

clean:
	./gradlew clean

reset-db:
	@echo "Resetting Postgres database schema..."
	$(CONTAINER) exec aionn-postgres psql -U postgres -d "aionn" -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	@echo "Flushing Redis cache..."
	$(CONTAINER) exec aionn-redis redis-cli -a hello FLUSHALL

up:
	$(CONTAINER) compose up -d

down:
	$(CONTAINER) compose down
