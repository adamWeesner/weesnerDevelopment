COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1 docker-compose -f backendRouter/docker-compose.yml build --no-cache
docker-compose --project-directory backendRouter up