# Autonomous Perception System - ONCE Dataset

Advanced Programming: Tasks 3 & 4

Comprehensive autonomous vehicle perception system with multi-modal sensor fusion, object detection, and enterprise-grade security validation integrated into CI/CD.

## Project Structure

```text
autonomous-perception-system/
├── src/
│   ├── main/java/com/moderndaytech/perception/
│   │   ├── AutonomousPerceptionSystem.java
│   │   ├── PerceptionResult.java
│   │   ├── dataset/
│   │   │   ├── ONCEDatasetLoader.java
│   │   │   └── ONCESceneAnnotation.java
│   │   ├── detection/
│   │   │   ├── ObjectDetectionEngine.java
│   │   │   ├── DetectedObject.java
│   │   │   ├── ObjectType.java
│   │   │   └── Position3D.java
│   │   ├── fusion/
│   │   │   ├── FusionAlgorithm.java
│   │   │   ├── SensorFusionProcessor.java
│   │   │   ├── KalmanFilterFusion.java
│   │   │   ├── ExtendedKalmanFilterFusion.java
│   │   │   ├── ParticleFilterFusion.java
│   │   │   └── WeightedAverageFusion.java
│   │   ├── security/
│   │   │   └── SecurityValidator.java
│   │   └── sensor/
│   │       ├── SensorData.java
│   │       ├── LiDARSensorData.java
│   │       ├── CameraSensorData.java
│   │       └── SensorType.java
│   ├── main/resources/
│   │   └── application.properties
│   └── test/java/com/moderndaytech/perception/
│       ├── dataset/
│       ├── detection/
│       ├── fusion/
│       ├── security/
│       └── sensor/
├── src/test/resources/test-scenes/
│   ├── vehicle_scene.json
│   ├── pedestrian_scene.json
│   ├── mixed_scene.json
│   └── empty_scene.json
├── Jenkinsfile
├── pom.xml
└── README.md
```

## Architecture Overview

The system processes autonomous perception data through a clear pipeline:

1. Sensor ingestion (`LiDARSensorData`, `CameraSensorData`)
2. Runtime input validation (`SecurityValidator`)
3. Sensor fusion (`SensorFusionProcessor` + pluggable `FusionAlgorithm`)
4. Object detection (`ObjectDetectionEngine`)
5. Unified output (`PerceptionResult`)

Key design points:

- ONCE-format annotations are parsed via `ONCEDatasetLoader`.
- Fusion algorithm is selected at runtime using dependency injection.
- Sensor models enforce constructor validation to prevent invalid object state.

## SOLID Principles Implementation

- **SRP**: Validation, fusion, detection, and data loading are separated by class responsibility.
- **OCP**: New fusion strategies (`ExtendedKalmanFilterFusion`, `WeightedAverageFusion`) are added without changing caller code.
- **LSP**: All `FusionAlgorithm` implementations work interchangeably in `SensorFusionProcessor`.
- **ISP**: Small focused interfaces/contracts are used (`FusionAlgorithm`, sensor abstractions).
- **DIP**: High-level orchestration depends on abstractions, not concrete fusion implementations.

## DevSecOps Pipeline

### CI/CD Flow

```text
Checkout → Build → SCA → SAST → Tests → Coverage → Security Gate → Package
```

### Pipeline Evidence (Single Source)

| Requirement                 | Pipeline Stage Name                    |
| --------------------------- | -------------------------------------- |
| Checkout Source             | `stage('Checkout')`                    |
| Build & Compilation         | `stage('Build')`                       |
| SCA (Dependency Check)      | `stage('SCA - Dependency Check')`      |
| SAST (SpotBugs)             | `stage('SAST - Static Analysis')`      |
| Unit Tests                  | `stage('Unit Tests')`                  |
| Integration Tests           | `stage('Integration Tests')`           |
| Runtime Security Validation | `stage('Runtime Security Validation')` |
| Code Coverage               | `stage('Code Coverage')`               |
| Security Gate               | `stage('Security Gate')`               |
| Package                     | `stage('Package')`                     |

GitHub Actions workflow: `.github/workflows/devsecops-live.yml`

Runtime security note:
As this system operates via Java method calls rather than HTTP endpoints, runtime attack injection into the `SecurityValidator` boundary serves as a DAST-equivalent approach. A production REST API deployment would add OWASP ZAP baseline scanning as an additional stage.

## Running the Tests

### Build

```bash
mvn clean compile
```

### Full test suite

```bash
mvn test
```

### Targeted suites

```bash
# Sensor/detection integration
mvn test -Dtest=ObjectDetectionEngineIntegrationTest

# Fusion integration
mvn test -Dtest=SensorFusionProcessorIntegrationTest

# ONCE dataset parsing + pipeline
mvn test -Dtest=ONCEDatasetLoaderTest,ONCEDatasetIntegrationTest

# Runtime security validation
mvn test -Dtest=AutonomousPerceptionSecurityTest
```

### Coverage report

```bash
mvn jacoco:report
```

Report location: `target/site/jacoco/index.html`

## Security Controls

Implemented controls include:

- SQL injection payload detection and blocking
- XSS payload detection and blocking
- Path traversal validation
- Data-size constraints to prevent memory exhaustion
- Constructor-level validation for core sensor models
- Runtime boundary validation via `SecurityValidator`

Primary security tests:

- `SecurityValidatorTest`
- `AutonomousPerceptionSecurityTest`

---

If you need a deep assessor appendix (traceability matrix mapping each acceptance criterion to file + test), it can be added as a separate `ASSESSMENT_EVIDENCE.md` to keep this README concise.
