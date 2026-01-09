# Resource Manager

**This is a complete AI generated project.**

A unified system to manage digital resources (Books, Videos, Music, Games, Pictures, etc.) across multiple locations and platforms.

## Architecture

- **Server**: Ktor (JVM) + SQLite + Exposed
- **Client**: Compose Multiplatform (Desktop)
- **Shared**: Kotlin Multiplatform (Common logic)

## Features

- **Centralized Management**: Track resources spread across NAS, cloud, and local drives.
- **Efficient Search**: Fast filtering and retrieval of item metadata.
- **Batch Operations**: Support for importing large collections via CSV/GZIP.
- **Cross-Platform**: Client runs on Windows, macOS, and Linux.

## Getting Started

### Prerequisites

- JVM 21+

### Running the Server

```bash
./gradlew server:run
```
Server runs on port `8080`.

### Running the Client

```bash
./gradlew composeApp:run
```

## Documentation

- [API Specification](docs/openapi.yaml)

## License

[MIT](LICENSE)

## Author

**Phil Lui**
- Email: [phillui37@gmail.com](mailto:phillui37@gmail.com)

