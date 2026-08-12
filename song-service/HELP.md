# Song Service

`song-service` stores song metadata linked to uploaded MP3 resources. It exposes CRUD-style HTTP endpoints, validates incoming metadata, and returns structured API errors.

## Base URL

- Local port: `8081`
- Base path: `/api/v1/songs`

## Features

- Create metadata for an uploaded resource
- Retrieve metadata by resource/song ID
- Delete metadata by comma-separated IDs
- Validate request payloads using Spring Validation
- Return JSON error responses with consistent status codes

## Endpoints

### Create song metadata

- **Method/Path:** `POST /api/v1/songs`
- **Consumes:** `application/json`
- **Produces:** `application/json`

#### Example request

```json
{
  "id": 1,
  "name": "Track name",
  "artist": "Artist name",
  "album": "Album name",
  "duration": "03:45",
  "year": "2024"
}
```

#### Success response

```json
{
  "id": 1
}
```

### Get song metadata

- **Method/Path:** `GET /api/v1/songs/{id}`
- **Produces:** `application/json`

#### Success response

```json
{
  "id": 1,
  "name": "Track name",
  "artist": "Artist name",
  "album": "Album name",
  "duration": "03:45",
  "year": "2024"
}
```

### Delete song metadata

- **Method/Path:** `DELETE /api/v1/songs?id=1,2,3`
- **Produces:** `application/json`

#### Success response

```json
{
  "ids": [1, 2, 3]
}
```

## Validation rules

### Request body (`POST /api/v1/songs`)

- `id`: required, positive number
- `name`: required, 1-100 characters
- `artist`: required, 1-100 characters
- `album`: required, 1-100 characters
- `duration`: required, `mm:ss` format
- `year`: required, between `1900` and `2099`

### Delete query parameter

- `id` must be a comma-separated list of positive integers
- Maximum length: `200` characters

## Error response format

### Validation error example (`400`)

```json
{
  "errorMessage": "name must not be blank"
}
```

### Bad request example (`400`)

```json
{
  "errorMessage": "Invalid request"
}
```

### Not found example (`404`)

```json
{
  "errorMessage": "Metadata for id=1 not found"
}
```

### Conflict example (`409`)

```json
{
  "errorMessage": "Metadata for id=1 already exists"
}
```

### Server error example (`500`)

```json
{
  "errorMessage": "An error occurred on the server"
}
```

## Configuration

`src/main/resources/application.properties`:

- `server.port=8081`
- `spring.datasource.url=jdbc:postgresql://localhost:5433/song_db`
- `spring.datasource.username=postgres`
- `spring.datasource.password=postgres`
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.jpa.open-in-view=false`

## Run locally

From the repository root:

```zsh
./gradlew :song-service:bootRun
```

Or from inside `song-service`:

```zsh
../gradlew bootRun
```

## Run tests

From the repository root:

```zsh
./gradlew :song-service:test
```

## Swagger UI

When the service is running:

- `http://localhost:8081/swagger-ui/index.html`

## Quick curl examples

Create metadata:

```zsh
curl -i -X POST "http://localhost:8081/api/v1/songs" \
  -H "Content-Type: application/json" \
  -d '{
	"id": 1,
	"name": "Track name",
	"artist": "Artist name",
	"album": "Album name",
	"duration": "03:45",
	"year": "2024"
  }'
```

Get metadata:

```zsh
curl -i "http://localhost:8081/api/v1/songs/1"
```

Delete metadata:

```zsh
curl -i -X DELETE "http://localhost:8081/api/v1/songs?id=1,2"
```

## Notes

- `song-service` works together with `resource-service`; resource uploads trigger metadata creation.
- `duration` should always be sent in `mm:ss` format with leading zeros.
- Errors are returned as JSON via `ApiExceptionHandler`.

