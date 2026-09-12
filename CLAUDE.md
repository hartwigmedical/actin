# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@../actin-claude/CLAUDE.md

## Build & Test Commands

```bash
# Build entire project
mvn clean install

# Build skipping tests
mvn clean install -DskipTests

# Build a specific module (and its dependencies)
mvn clean install -pl report -am

# Run all tests
mvn test

# Run all tests in a specific module
mvn test -pl algo

# Run a specific test class
mvn test -pl report -Dtest=ReportWriterTest

# Run a specific test method
mvn test -pl algo -Dtest=HasWHOStatusTest#'Should pass for sufficient WHO status'
```

## Architecture Overview

**actin** determines available treatment options (standard-of-care and clinical trials) for cancer patients, based on: a comprehensive
clinical record of the patient, a comprehensive molecular analysis of the tumor, and the set of all treatment options available.
Pipeline: **Trial config (`trial`) + Clinical record + Molecular interpretation (`molecular`) → Algo (`algo`,
eligibility matching) → Report (`report`)**.

### Module Map

| Module           | Purpose                                                                                                                                                                                                                                                                                                      |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `common`         | Shared domain code used across modules: `ReportConfiguration`, DOID model, ICD codes, medication/calendar utilities, personalization, and the `EligibilityRule` enum. Core types (`PatientRecord`, `Evaluation`, trial/molecular models) come from the external `actin-datamodel` artifact, not this module. |
| `trial`          | Ingests trial configuration files into structured `Trial`/`Eligibility` objects (`EligibilityFactory`, `TrialIngestion`) consumed by `algo`.                                                                                                                                                                 |
| `molecular`      | Maps molecular tests of any type (IHC, panel NGS, WGS) to the ACTIN molecular datamodel, annotates findings with literature-based treatment evidence and external trials, and merges the result with clinical data into the final patient record.                                                            |
| `algo`           | The eligibility matching engine (`TreatmentMatcherApplication`): evaluates every trial's in-/exclusion criteria against a patient record.                                                                                                                                                                    |
| `interpretation` | Adapter layer that reshapes `algo`/`trial`/`molecular` output into report-ready types (e.g. `InterpretedCohort`) for the `report` module.                                                                                                                                                                    |
| `report`         | Generates the PDF report (iText-based, `ReporterApplication`) showing available treatment options.                                                                                                                                                                                                           |
| `database`       | jOOQ-based persistence: loads clinical/molecular/trial/treatment-match data into a MySQL database, schema in `database/src/main/resources/generate_database.sql`.                                                                                                                                            |
| `system`         | Module which bundles everything and adds example patients and reports + regression tests                                                                                                                                                                                                                     |

### Production Orchestration

In production, `molecular`, `algo`, and `report` aren't just run manually — each is packaged as a versioned Docker image and run as a
pipeline stage (`molecular` → `patient_record.json`, `algo` → `treatment_match.json`, `report` → PDF) by the sibling
`actin-pipelines` repo's `analysis-pipeline` module (`AnalysisPipelineLauncher`). That pipeline subscribes to the GCP Pub/Sub event
`ActinAnalysisPipelinePending` (fired on a verified WGS run, a clinical/trial update, or manually) and publishes
`ActinAnalysisPipelineComplete` when done; stage image versions are pinned independently (e.g. an `actin_algo_version` param). See
`actin-pipelines/CLAUDE.md` for that repo's own patterns (`PipelineLauncher<Event>`, Nextflow launcher for bioinformatics steps, Pub/Sub
event bus) — this repo only produces the artifacts those stages run.

### Key Patterns

**Rule-based eligibility evaluation** — The `algo` module's core abstraction is `EvaluationFunction` (
`fun evaluate(record: PatientRecord): Evaluation`), implemented by classes under `algo/evaluation/<category>/` (e.g.
`HasWHOStatus`). Each criterion resolves to `PASS`/`WARN`/`FAIL`/`UNDETERMINED` and is separately tagged 'recoverable' or 'unrecoverable' (
e.g. a lab value can improve on retest; a primary tumor location cannot). Rules combine via composite functions `AND`/`OR`/`NOT`/`WARN_IF`.
`RuleMapper` maps `EligibilityRule` enum values to a `FunctionCreator`; `EvaluationFactory` provides standardized result constructors.

**Config + Application pairing (Apache Commons CLI)** — Every runnable module has a `*Application` class paired with a `*Config` data class
parsed via `org.apache.commons.cli` (not picocli, despite the org-wide convention doc), e.g.
`ReporterApplication -patient_json ... -treatment_match_json ... -output_directory ...`.

**jOOQ-generated persistence, no ORM** — `database` generates jOOQ code at build time from a DDL source, then hand-written DAO classes (
`ClinicalDAO`, `TrialDAO`, `MolecularDAO`, `TreatmentMatchDAO`) wrap the generated tables directly.

**Shared external datamodel** — `PatientRecord`, `Evaluation`, and trial/molecular types come from the versioned external `actin-datamodel`
artifact (root `pom.xml`), not module-local definitions. `EligibilityRule` is an exception: it's defined locally in `common`.

**Centralized theming in `report`** — `Styles` (fonts/colors/sizes per semantic role) and `Cells` (styled cell factories) keep all PDF
visual styling in two files; chapters implement `ReportChapter`, tables implement `TableGenerator`.

**Visual + textual PDF regression testing** — `system`'s `ReportRegressionTest` regenerates example reports (`LUNG-01`, `CRC-01`, defined in
`system/src/test/resources/example_patient_data`) and asserts the output PDF is textually *and visually* identical to the checked-in
baseline in `system/src/test/resources/example_reports/`.

## Tech Stack

- **Language**: Kotlin 2.0.0, JVM 17
- **Build**: Maven (multi-module)
- **JSON**: Jackson with Kotlin module
- **Testing**: JUnit 5, AssertJ, MockK
- **PDF generation**: iText
- **Database**: MySQL via jOOQ (generated code + hand-written DAOs)
- **Logging**: kotlin-logging