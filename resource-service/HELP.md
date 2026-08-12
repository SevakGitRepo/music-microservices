# Resource Service

`resource-service` stores MP3 binaries, extracts metadata with Apache Tika, and sends metadata to `song-service`.

## Base URL and API prefix

- Local port: `8080`
- Base path: `/api/v1/resources`

## Endpoints

### 1) Upload MP3

- **Method/Path:** `POST /api/v1/resources`
- **Consumes:** `audio/mpeg`
- **Produces:** `application/json`
- **Success response:**

```json
{
  "id": 123
}
```

### 2) Get MP3 by ID

- **Method/Path:** `GET /api/v1/resources/{id}`
- **Success response:** MP3 binary with `Content-Type: audio/mpeg`
- **Error response:** JSON (`application/json`)

### 3) Delete resources by CSV IDs

- **Method/Path:** `DELETE /api/v1/resources?id=1,2,3`
- **Produces:** `application/json`
- **Success response:**

```json
{
  "ids": [1, 2, 3]
}
```

## Error response format

### Validation error (`400`)

```json
{
  "errorMessage": "Validation error",
  "details": {
	"duration": "Duration must be in mm:ss format with leading zeros",
	"year": "Year must be between 1900 and 2099"
  },
  "errorCode": "400"
}
```

### Not found (`404`)

```json
{
  "errorMessage": "Resource with id=100 not found",
  "errorCode": "404"
}
```

### Internal server error (`500`)

```json
{
  "errorMessage": "An error occurred on the server",
  "errorCode": "500"
}
```

## Configuration

`src/main/resources/application.properties`:

- `server.port=8080`
- `song.service.base-url=http://localhost:8081`
- `song.service.metadata-endpoint=/api/v1/songs`
- `spring.datasource.url=jdbc:postgresql://localhost:5432/resource_db`
- `spring.datasource.username=postgres`
- `spring.datasource.password=postgres`
- `spring.jpa.hibernate.ddl-auto=update`

## Run locally

From repo root:

```zsh
./gradlew :resource-service:bootRun
```

Or from `resource-service` directory:

```zsh
../gradlew bootRun
```

## Run tests

From repo root:

```zsh
./gradlew :resource-service:test
```

## OpenAPI / Swagger UI

When service is running:

- `http://localhost:8080/swagger-ui/index.html`

## Quick curl examples

Upload:

```zsh
curl -i -X POST "http://localhost:8080/api/v1/resources" \
  -H "Content-Type: audio/mpeg" \
  --data-binary "@/absolute/path/to/file.mp3"
```

Get binary:

```zsh
curl -i "http://localhost:8080/api/v1/resources/1" -o resource-1.mp3
```

Delete:

```zsh
curl -i -X DELETE "http://localhost:8080/api/v1/resources?id=1,2"
```

