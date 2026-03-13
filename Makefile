ifeq ($(OS),Windows_NT)
GRADLEW := .\\gradlew.bat
else
GRADLEW := ./gradlew
endif

CONTAINER := podman

.PHONY: build test run clean up down logs

build:
	$(GRADLEW) build -x test

test:
	$(GRADLEW) test

run:
	$(GRADLEW) :app:bootRun

clean:
	$(GRADLEW) clean

up:
	$(CONTAINER) compose up -d

down:
	$(CONTAINER) compose down

logs:
	$(CONTAINER) compose logs -f
