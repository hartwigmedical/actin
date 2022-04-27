## ACTIN-Treatment

ACTIN-Treatment creates a database of potential treatments for [ACTIN-Algo](../algo/README.md) to match against.  
The application takes in a set of configuration files and writes a database in JSON format to a specified output directory. 

This application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.treatment.TreatmentCreatorApplication \
   -trial_config_directory /path/to/trial_config_dir
   -output_directory /path/to/where/treatment_json_files/are/written
```

### Configuration of trials in the treatment database

Trials are read from the `trial_config_directory`. The following files are expected to be present in this directory:
 - `trial_definition.tsv` defining all trials that can be matched against.
 - `cohort_definition.tsv` defining all cohorts within the set of trials that can be matched against.
 - `inclusion_criteria.tsv` defining all inclusion criteria for the trials and cohorts
 - `inclusion_criteria_reference.tsv` providing (external) reference texts for the inclusion criteria.
 
The treatment database will only be created if the trial config is entirely consistent and can be resolved to interpretable rules. 
In case of any configuration issue, this application will crash while providing details on how to solve the configuration issue(s).

An example trial configuration database can be found [here](src/test/resources/trial_config) 
 
#### Trial Definition

Field | Example
---|---
trialId	| ACTN 2021
open | 1
acronym	| ACTIN
title | ACTIN is a study to evaluate a new treatment decision system  
 
The following checks are done on the level of trial definitions:
 - Every trial ID must be unique
 - Every file identifier generated for any trial must be unique. The file identifier is the trial ID with spaces replaced by underscores.

#### Cohort Definition

Field | Example
---|---
trialId | ACTN 2021
cohortId | A
open | 1
slotsAvailable | 1
blacklist | 0
description | First evaluation phase

The following checks are done on the level of cohort definitions:
 - Every trial ID referenced from a cohort must be defined in the trial definitions file
 - Every cohort ID must be unique within the context of a specific trial

#### Inclusion Criteria

Field | Example
---|---
trialId | ACTN 2021
referenceIds | I-01, I-02
appliesToCohorts | all
inclusionRule | AND(IS_AT_LEAST_X_YEARS_OLD[18], HAS_METASTATIC_CANCER)

The following checks are done on the level of inclusion criteria:
 - Every trial ID referenced in an inclusion criterion must be defined in the trial definition file
 - Every reference ID must be defined in the inclusion criteria reference file.
 - Every cohort ID in the comma-separated list of cohorts must be defined in the cohort definition file, unless this field has been set to `all` cohorts.
 - The inclusion rule has to be valid according to [inclusion rule configuration](#inclusion-rule-configuration)
 
#### Inclusion Criteria Reference

Field | Example
---|---
trialId | ACTN 2021
referenceId | I-01
referenceText | Patient has to be 18 years old

The following checks are done on the level of inclusion criteria references:
 - Every trial ID referenced in a criteria reference must be defined in the trial definition file.
 - Every reference ID must be unique within the context of a specific trial.

### Inclusion rule configuration

An inclusion rule has to be either a specific rule or a composite rule combining specific rules defined in [ACTIN-Algo](../algo/README.md).
 - Inputs to composite rules are expected to be enclosed in parentheses (for example `NOT(SPECIFIC_RULE)`).
 - Inputs to specific rules are expected to be enclosed in brackets (for example `SPECIFIC_RULE_WITH_ONE_PARAM[2]`).
 - A list of inputs to rules are expected to be separated by semicolons (for example `HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y[2, NEUROPATHY;THYROID]`).

An inclusion rule is configured validly only if the expected number of inputs is provided in the config and the inputs can be resolved to the 
type of input expected by the function. 

Some examples:

Human readable rule | How to configure as a inclusion rule
---|---
Patient has to be an adult | IS_AT_LEAST_X_YEARS_OLD[18]
Has a maximum total bilirubin of 2.5 ULN, or 5.0 ULN in case patient has Gilbert's disease | OR(HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X[2.5], AND(HAS_GILBERT_DISEASE, HAS_DIRECT_BILIRUBIN_ULN_AT_MOST_X[5]))
Patient has no active CNS metastases | NOT(HAS_ACTIVE_CNS_METASTASES)

### Version History and Download Links
 - Upcoming (first release) 