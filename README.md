# Widget-service

Miro test task for a backend developer

Swagger available on `http://localhost:8080/swagger-ui.html`

`open-api.yml` in resource package

Request examples in `request-examples` folder in root

Liquibase enabled on h2-database

There are 2 profiles `memory` and `database`. 
You could change by editing `application.properties` file. 

In order to achieve less than **O(n)** when searching for given coordinates, I chose R-tree structure for `memory` implementation and made indexes for `database` implementation.