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
   -serve_db_json /path/to/serve_db_json_file \
   -output_directory /path/to/where/treatment_json_files/are/written
```

### Configuration of trials in the trial database

Trials are read from the `trial_config_directory`. The following files are expected to be present in this directory:

- `trial_definition.tsv` defining all trials that can be matched against.
- `cohort_definition.tsv` defining all cohorts within the set of trials that can be matched against.
- `inclusion_criteria.tsv` defining all inclusion criteria for the trials and cohorts
- `inclusion_criteria_reference.tsv` providing (external) reference texts for the inclusion criteria.

The trial database will only be created if the trial config is entirely consistent and can be resolved to interpretable rules.
In case of any configuration issue, this application will crash while providing details on how to solve the configuration issue(s).

An example trial configuration database can be found [here](src/test/resources/trial_config)

Note the trial status database referred to in the configuration files is either the CTC or NKI database, depending on configuration.

#### Trial Config

| Field             | Example                                                       | Notes                                                          |
|-------------------|---------------------------------------------------------------|----------------------------------------------------------------|
| trialId           | ACTN 2021                                                     |                                                                |
| nctId             | The NCT (identifier used by clinicaltrials.gov) of the trial. | Optional, but most trials should have an NCT id.               |
| open              | true                                                          |                                                                |
| acronym           | ACTIN                                                         |                                                                |
| title             | ACTIN is a study to evaluate a new treatment decision system  |                                                                |
| source            | EMC, NKI or LKO                                               |                                                                |
| phase             | PHASE_1                                                       |                                                                |
| locations         | EMC, NKI                                                      |                                                                |
| cohorts           |                                                               | List of cohorts configs, see below.                            |
| inclusionCriteria |                                                               | List of inclusion criteria which apply to all cohorts in trial |


#### Cohort Config

| Field             | Example                | Notes                                                            |
|-------------------|------------------------|------------------------------------------------------------------|
| cohortId          | A                      |                                                                  |  
| open              | true                   |                                                                  | 
| slotsAvailable    | true                   |                                                                  |
| ignore            | false                  | Use for cohorts requested to be ignored by requesting hospital   |
| evaluable         | true                   | Use for cohorts with incomplete or undefined inclusion criteria. |
| description       | First evaluation phase |                                                                  |
| inclusionCriteria |                        | List of inclusion criteria that apply only to this cohort.       |

#### Inclusion Criteria

| Field         | Example                                                 | Notes                                      |
|---------------|---------------------------------------------------------|--------------------------------------------|
| inclusionRule | AND(IS_AT_LEAST_X_YEARS_OLD[18], HAS_METASTATIC_CANCER) |                                            |
| references    |                                                         | List of criteria references for this rule. |


#### Inclusion Criteria Reference

| Field | Example                        |
|-------|--------------------------------|
| id    | I-01                           |
| text  | Patient has to be 18 years old |

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