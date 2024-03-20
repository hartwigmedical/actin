## ACTIN-Trial

ACTIN-Trial creates a database of potential trials for [ACTIN-Algo](../algo/README.md) to match against.  
The application takes in a set of configuration files and writes a database in JSON format to a specified output directory.

This application requires Java 11+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.trial.TrialCreatorApplication \
   -ctc_config_directory /path/to/ctc_config_dir \
   -trial_config_directory /path/to/trial_config_dir \
   -treatment_directory /path/to/treatment_dir \
   -doid_json /path/to/full_doid_tree_json_file \
   -known_genes_tsv /path/to/known_genes.tsv \
   -output_directory /path/to/where/treatment_json_files/are/written
```

### CTC trial database ###

[The EMC Clinical Trial Center (CTC)](https://www.ctc-erasmusmc.nl) provides a database of trials, cohorts and associated status
for ACTIN to ingest. Their database is delivered in a tsv file `ctc_database.tsv` and can be ingested in ACTIN-Trial to resolve trial
and cohort states. In addition, the CTC database is used for ACTIN to become aware of new or closed trials and cohorts.

Along with the CTC database itself, the `ctc_config_directory` is expected to contain three additional files:

- `ignore_studies.tsv`: A single-column file with studies that are present in the CTC database but ACTIN explicitly should ignore
- `unmapped_cohorts.tsv`: A single-colum file with cohort IDs that are present in the CTC database but ACTIN explicitly doesn't map to
  internal cohorts.
- `studies_not_in_ctc.tsv`: A single-column file with studies that have a MEC ID but are not present in the CTC database, for which the manual configured status is used

The CTC database is used to resolve trial and cohorts states as described in the next section, and in addition the following checks are
performed on the CTC database itself as well as the two additional configuration files:

- Any study that is configured in `ignore_studies` is checked on actually being present in the CTC database.
- Any cohort that is configured un `unmapped_cohorts` is checked on actually being present in the CTC database.
- Every study that is present in the CTC database should either be present in `ignore_studies` or actually be referenced in
  the `trial_definition` overview as described below.
- Every cohort that is present in the CTC database for studies that are not ignored should either be:
    - Unmapped via configuration in `unmapped_cohorts`
    - Referenced directly in the `cohort_definition` overview described below.
    - A child of a cohort that is explicitly referenced in the `cohort_definition`

An example CTC config database can be found [here](src/test/resources/ctc_config)

### NKI trial database ###

The NKI also provides us with updates on trial and cohort status . Their database is delivered in a json file `trial_status.json`, and otherwise 
behaves identically to the CTC database.

Given that, with the NKI json itself, the `nki_config_directory` is expected to contain the same three additional files as above.

The additional checks described above for the CTC database are also performed on the NKI database.

### Configuration of trials in the trial database

Trials are read from the `trial_config_directory`. The following files are expected to be present in this directory:

- `trial_definition.tsv` defining all trials that can be matched against.
- `cohort_definition.tsv` defining all cohorts within the set of trials that can be matched against.
- `inclusion_criteria.tsv` defining all inclusion criteria for the trials and cohorts
- `inclusion_criteria_reference.tsv` providing (external) reference texts for the inclusion criteria.

The trial database will only be created if the trial config is entirely consistent and can be resolved to interpretable rules.
In case of any configuration issue, this application will crash while providing details on how to solve the configuration issue(s).

An example trial configuration database can be found [here](src/test/resources/trial_config)

#### Trial Definition

| Field   | Example                                                      | Notes                                                         |
|---------|--------------------------------------------------------------|---------------------------------------------------------------|
| trialId | ACTN 2021                                                    |                                                               |
| open    | 1                                                            | Optional (only required in case a study isn't present in CTC) |
| acronym | ACTIN                                                        |                                                               |
| title   | ACTIN is a study to evaluate a new treatment decision system |                                                               |

The following checks are done on the level of trial definitions:

- Every trial ID must be unique
- Every file identifier generated for any trial must be unique. The file identifier is the trial ID with spaces replaced by underscores.

The `open` status for a trial is resolved from the CTC trial database via taking the status of one of the cohorts of the trial.
In case the set of cohort states for a single study are inconsistent, a warning is raised, and it is assumed the trial is closed.

#### Cohort Definition

| Field          | Example                | Notes                                                                  |
|----------------|------------------------|------------------------------------------------------------------------|
| trialId        | ACTN 2021              |                                                                        |
| cohortId       | A                      |                                                                        |
| ctcCohortIds   | 462;463                |                                                                        | 
| open           | 1                      | Optional (only required in case cohort is not present in CTC database) | 
| slotsAvailable | 1                      | Optional (only required in case cohort is not present in CTC database) |
| blacklist      | 0                      |                                                                        |
| description    | First evaluation phase |                                                                        |

The following checks are done on the level of cohort definitions:

- Every trial ID referenced from a cohort must be defined in the trial definitions file
- Every cohort ID must be unique within the context of a specific trial
- The `ctcCohortIds` value needs to be one of the following:
    - a single parent cohort ID from the CTC database.
    - One or more children cohort IDs from the CTC database, combined via `;`
    - One of the following specific values, requiring manual configuring of `open` and `slotsAvailable`:
        - `NA`: The cohort is not part of a study managed by CTC
        - `not_in_ctc_overview_unknown_why`: The cohort is missing from CTC even though it should be present.
        - `overruled_because_incorrect_in_ctc`: The data is wrong from CTC and configuration is overruled manually.
    - One of the following specific values, in which case it is assumed the cohort is closed without slots available:
        - `wont_be_mapped_because_closed`: In case a trial is closed but still present in ACTIN's trial database.
        - `wont_be_mapped_because_not_available`: The cohort will never be mapped by CTC since the cohort is unavailable within CTC.

The `open` and `slotsAvailable` are resolved from the CTC database in case `ctcCohortIds` is configured as one parent cohort ID or one or
more child cohort IDs. The following logic is applied:

- If a single parent cohort ID is configured, ACTIN reads the `open` and `slotsAvailable` status from exactly that entry in the CTC database
- If one or more children are configured, ACTIN takes the "most lenient" option. Cohorts are more lenient if they are open and have slots
  available. An extra check is done in case the best entry is more lenient than its parent (e.g. parent cohort closed while child open).
  This check results in a warning, but it is assumed the most lenient child is still representative of the actual cohort status.

#### Inclusion Criteria

| Field            | Example                                                 |
|------------------|---------------------------------------------------------|
| trialId          | ACTN 2021                                               |
| referenceIds     | I-01, I-02                                              |
| appliesToCohorts | all                                                     |
| inclusionRule    | AND(IS_AT_LEAST_X_YEARS_OLD[18], HAS_METASTATIC_CANCER) |

The following checks are done on the level of inclusion criteria:

- Every trial ID referenced in an inclusion criterion must be defined in the trial definition file
- Every reference ID must be defined in the inclusion criteria reference file.
- Every cohort ID in the comma-separated list of cohorts must be defined in the cohort definition file, unless this field has been set
  to `all` cohorts.
- The inclusion rule has to be valid according to [inclusion rule configuration](#inclusion-rule-configuration)

#### Inclusion Criteria Reference

| Field         | Example                        |
|---------------|--------------------------------|
| trialId       | ACTN 2021                      |
| referenceId   | I-01                           |
| referenceText | Patient has to be 18 years old |

The following checks are done on the level of inclusion criteria references:

- Every trial ID referenced in a criteria reference must be defined in the trial definition file.
- Every reference ID must be unique within the context of a specific trial.

### Inclusion rule configuration

An inclusion rule has to be either a specific rule or a composite rule combining specific rules defined in [ACTIN-Algo](../algo/README.md).

- Inputs to composite rules are expected to be enclosed in parentheses (for example `NOT(SPECIFIC_RULE)`).
- Inputs to specific rules are expected to be enclosed in brackets (for example `SPECIFIC_RULE_WITH_ONE_PARAM[2]`).
- A list of inputs to rules are expected to be separated by comma's (for
  example `HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y[2, NEUROPATHY;THYROID]`).

An inclusion rule is configured validly only if the expected number of inputs is provided in the config and the inputs can be resolved to
the type of input expected by the function. This is where the additional inputs to ACTIN-treatment come into play:

- `treatment_directory` is used as a reference to check that any configured treatment is actually a treatment
- `doid_json` holds the DOID tree and is used to verify that any configured DOID actually exists
- `known_genes_tsv` defines the set of genes known to play a role in cancer, and any gene configured in the trial config should be present
  in this set.

Some examples:

| Human readable rule                                                                        | How to configure as a inclusion rule                                                                       |
|--------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| Patient has to be an adult                                                                 | IS_AT_LEAST_X_YEARS_OLD[18]                                                                                |
| Has a maximum total bilirubin of 2.5 ULN, or 5.0 ULN in case patient has Gilbert's disease | OR(HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X[2.5], AND(HAS_GILBERT_DISEASE, HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X[5])) |
| Patient has no active CNS metastases                                                       | NOT(HAS_ACTIVE_CNS_METASTASES)                                                                             |

### Version History and Download Links

- Upcoming (first release) 