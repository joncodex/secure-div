# Secure-Div

A Spring Boot REST API for issuing and verifying tamper-proof digital certificates and transcripts for educational institutions.

## What It Does

Secure-Div allows institutions to:
- Generate PDF certificates and transcripts from HTML templates
- Issue documents with QR codes for public verification
- Provide time-limited, presigned download links sent via email
- Verify document authenticity using SHA-256 file integrity checks
- Revoke documents with reason tracking
- Log all document access for audit purposes
- Send webhook notifications when documents are issued

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.5.11 (Java 21) |
| Database | MySQL + Spring Data JPA (Hibernate) |
| PDF Generation | Microsoft Playwright (headless Chromium) + Thymeleaf |
| File Storage | AWS S3-compatible (MinIO for dev, Cloudflare R2 / AWS S3 for prod) |
| QR Codes | Google ZXing |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

## Prerequisites

- Java 21+
- Maven 3.8+
- MySQL database
- S3-compatible storage (MinIO locally, or Cloudflare R2 / AWS S3)
- Playwright Chromium browser (for PDF generation)

## Environment Variables

Create a `.env` file in the project root with the following:

```env
DB_URL=jdbc:mysql://localhost:3306/securediv
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

MIN_ENDPOINT=http://localhost:9000
MIN_ACCESS_KEY_ID=your_access_key
MIN_SECRET_ACCESS_KEY=your_secret_key
MIN_BUCKET_NAME=your_bucket_name
MIN_REGION=us-east-1

BASE_URL=http://localhost:8080

WEBHOOK_SECRET=your_hmac_secret
WEBHOOK_URL=https://your-webhook-receiver.com/endpoint
```

## Running the Application

```bash
# Clone the repository
git clone <repo-url>
cd secure-div

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.
Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

## API Overview

All endpoints are prefixed with `/api/v1`.

### Students — `/api/v1/students`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/create` | Register a new student |
| GET | `/{studentId}` | Get student details |
| GET | `/all-student` | List all students |
| PUT | `/update` | Update student information |
| DELETE | `/delete/{studentId}` | Delete a student |

### Certificates — `/api/v1/certificates`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/create` | Issue a new certificate (generates PDF, uploads to S3) |
| GET | `/{documentNumber}` | Get certificate details |
| GET | `/verify/{documentNumber}` | Publicly verify a certificate |
| POST | `/download` | Request a download link (validates email, logs access) |
| GET | `/download/{token}` | Download the certificate PDF |
| PUT | `/revoke` | Revoke a certificate |

### Transcripts — `/api/v1/transcripts`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/create` | Issue a new transcript |
| GET | `/{documentNumber}` | Get transcript details |
| GET | `/verify/{documentNumber}` | Publicly verify a transcript |
| POST | `/download` | Request a download link |
| GET | `/download/{token}` | Download the transcript PDF |
| PUT | `/revoke` | Revoke a transcript |

### Course Results — `/api/v1/course-results`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/create` | Add a course result |
| GET | `/{studentId}/{courseCode}` | Get a specific result |
| PUT | `/{studentId}/{courseCode}` | Update a result |
| DELETE | `/{studentId}/{courseCode}` | Delete a result |

### Institutions — `/api/v1/institutions`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/create` | Register or update institution record |
| GET | `/current` | Get current institution details |

### Signatories — `/api/v1/signatories`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/create` | Register a new signatory |
| GET | `/current` | Get current active signatories |
| PUT | `/invalidate/{name}` | Invalidate a signatory |

## Key Design Decisions

- **Single Table Inheritance**: Certificates and Transcripts share a single `documents` table with a `doc_type` discriminator column.
- **Presigned Download URLs**: Download tokens are Base64-encoded and map to 15-minute S3 presigned URLs — S3 paths are never exposed directly.
- **File Integrity**: SHA-256 hashes are stored at document creation and verified before serving download URLs.
- **Async Access Logging**: Document access is logged asynchronously to avoid blocking download responses.
- **Email Domain Validation**: Download requests reject common personal email providers (Gmail, Yahoo, etc.) — only institutional emails are accepted.
- **Webhook Signing**: Outbound webhooks are signed with HMAC-SHA256 using `WEBHOOK_SECRET`.
- **Pessimistic Locking**: Institution and signatory creation use `PESSIMISTIC_WRITE` locks to prevent race conditions.

## Project Structure

```
src/main/java/com/enzelascripts/securediv/
├── controller/       # REST controllers (7 classes)
├── service/          # Business logic (14 classes)
├── repository/       # JPA repositories (8 interfaces)
├── entity/           # JPA entities (8 classes)
├── request/          # Request DTOs (9 classes)
├── response/         # Response DTOs
├── exception/        # Custom exceptions (16 classes)
├── config/           # Spring configuration (S3, beans)
├── security/         # Security configuration (placeholder)
├── util/             # Utility functions
├── validation/       # Custom validators
├── dto/              # Shared DTOs (e.g. WebhookPayload)
└── annotation/       # Custom annotations

src/main/resources/
├── templates/        # Thymeleaf HTML templates for certificate and transcript PDFs
├── db/migration/     # Flyway migrations (currently disabled)
└── application.properties
```

## Notes

- Flyway migrations are configured but disabled (`spring.flyway.enabled=false`). Hibernate manages the schema via `ddl-auto=update`.
- Spring Security is not yet implemented — all endpoints are currently open. Authentication and authorization should be added before production deployment.
- The `EmailService` is a stub and does not yet send actual emails.
