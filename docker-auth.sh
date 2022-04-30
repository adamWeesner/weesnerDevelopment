COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1 docker-compose -f auth/docker-compose.yml build --no-cache
docker-compose --project-directory auth up