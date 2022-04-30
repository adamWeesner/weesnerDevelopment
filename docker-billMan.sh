COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1 docker-compose -f billMan/docker-compose.yml build --no-cache
docker-compose --project-directory billMan up