### CI Setup

The CICD setup for all of WeesnerDevelopment uses Github Actions. Currently the CD process is manual pushing to the
local server.

#### There are a few steps needed to get the ci pipeline up and going for a Gradle module.

- you need to create a `{module name}-service.yml` file for the Gradle module
- the module must have a `Dockerfile` and a `docker-compose.yml` file

#### The CD pipeline being manual has a few steps as well.

- make sure the CI pipeline has run with the latest changes for the Gradle module being added on the server
- go to portainer
- create a new stack pointing to the docker image for the service being added
- add a new dns record in Cloudflare if needed (be sure to not proxy it at first or the SSL Cert will not work)
- login to NGINX and add a proxy host for the new service, make sure to create a SSL Cert for it
- go back to Cloudflare and enable "Proxied"
- the service should now be available :)