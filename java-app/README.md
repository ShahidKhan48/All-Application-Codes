# Ninja Workflow Scheduler

A workflow scheduling service built with Spring Boot 3 and JobRunr, using MongoDB for persistent storage. This service is specifically designed for scheduling and managing workflow executions.

## Architecture

This project follows **Hexagonal Architecture** (Ports and Adapters) pattern with the following structure:

- **`app`**: Application layer containing REST controllers and DTOs
- **`core`**: Domain layer containing business logic, interfaces, and domain models
- **`infra`**: Infrastructure layer containing external integrations (JobRunr, MongoDB)

## Features

- **Recurring Workflow Scheduling**: Schedule workflows with CRON expressions
- **Workflow Management**: Update, delete, and discontinue scheduled workflows
- **Automatic Endpoint Resolution**: Workflow endpoints are automatically built from workflow names
- **Persistent Storage**: All schedules are persisted in MongoDB

## Prerequisites

- Java 21
- Maven 3.6+
- MongoDB 4.4+ (running locally or accessible)

## Configuration

### MongoDB

Configure MongoDB connection via environment variables or `application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/jobrunr
spring.data.mongodb.database=jobrunr
```

Or via environment variables:
- `MONGODB_URI` (default: `mongodb://localhost:27017/jobrunr`)
- `MONGODB_DATABASE` (default: `jobrunr`)

### JobRunr

JobRunr is configured to use MongoDB as its storage backend. The dashboard is enabled by default on port 8000.

Configuration options:
- `jobrunr.background-job-server.enabled=true` - Enable background job processing
- `jobrunr.dashboard.enabled=true` - Enable web dashboard
- `jobrunr.dashboard.port=8000` - Dashboard port

### Workflow Endpoint

Configure the base URL for workflow endpoints:

```properties
scheduler.workflow.endpoint-base=https://api.trafyn.info/workflow-engine/...
```

Or via environment variable:
- `WORKFLOW_ENDPOINT_BASE`

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using Environment Variables

```bash
MONGODB_URI='mongodb://localhost:27017/jobrunr' \
MONGODB_DATABASE=jobrunr \
mvn spring-boot:run
```

## API Endpoints

### Schedule Recurring Workflow

```http
POST /api/workflows/recurring
Content-Type: application/json

{
  "jobName": "workflow-name",
  "cronExpression": "0 */5 * * * *",
  "payload": {
    "key": "value"
  }
}
```

### Delete Recurring Workflow

```http
DELETE /api/workflows/recurring/{workflowName}
```

## JobRunr Dashboard

Access the JobRunr dashboard at: `http://localhost:8000`

The dashboard provides:
- Overview of all jobs (enqueued, processing, succeeded, failed)
- Job details and execution history
- Recurring jobs management
- Background job server status

## Project Structure

```
src/main/java/com/nc/scheduler/
├── app/                                  # Application layer
│   ├── controller/                       # REST controllers
│   ├── dto/                              # Data Transfer Objects
│   └── exception/                        # API error models and handlers
├── core/                                 # Domain layer
│   └── domain/
│       ├── exception/                    # Domain-specific exceptions
│       ├── port/
│       │   ├── in/                       # Incoming ports (use cases)
│       │   └── out/                      # Outgoing ports (driven adapters)
│       ├── service/                      # Domain services
│       └── util/                         # Domain utilities and enums
└── infra/                                # Infrastructure layer
    └── adapter/
        └── jobrunr/                      # JobRunr adapters (execution & scheduling)
```

## Architecture Details

The service follows hexagonal architecture principles:

1. **Ports**: `WorkflowSchedulePort` defines the workflow scheduling use cases
2. **Adapters**: `JobRunrScheduleAdapter` implements the port using JobRunr
3. **Domain Logic**: Workflow endpoint URLs are built automatically from workflow names

## Development

### Building

```bash
mvn clean package
```

### Testing

```bash
mvn test
```

## License

Copyright © Ninja Workflow
