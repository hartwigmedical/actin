## ACTIN-Treatment

ACTIN-Treatment creates a database of potential treatments that [ACTIN-Algo](../algo/README.md) matches against.  
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
 - `inclusion_criteria_reference.tsv` providing reference texts for the inclusion criteria themselves.
 
The treatment database will only be created if the trial config is entirely consistent and can be resolved to the interpretable rules. 
In case of any configuration issue, this application will crash while providing details on how to solve the configuration issue.

An example trial configuration database can be found [here](src/test/resources/trial_config) 
 
#### Trial Definition

Field | Example
---|---
trialId	| ACTN 2021
acronym	| ACTIN
title | ACTIN is a study to evaluate a new treatment decision system  
 
The following checks are done on the level of trial definitions:
 - Every trialId must be unique
 - Every file identifier generated for any trial must be unique.

#### Cohort Definition

Field | Example
---|---
trialId | ACTN 2021
cohortId | A
open | 1
description | First evaluation phase

The following checks are done on the level of cohort definitions:
 - Every trial referenced from a cohort must be defined in the trial definitions file
 - Every cohortId must be unique within the context of a specific trial

#### Inclusion Criteria

Field | Example
---|---
trialId | ACTN 2021
referenceIds | I-01, I-02
appliesToCohorts | all
inclusionRule | AND(IS_AT_LEAST_18_YEARS_OLD, HAS_METASTATIC_CANCER)

The following checks are done on the level of inclusion criteria:
 - Every trial referenced in an inclusion criterion must be defined in the trial definition file
 - The comma-separated list of reference Ids must not be empty.
 - Every reference must be defined in the inclusion criteria reference file.
 - The criteria must apply to the comma-separated list of cohorts defined in the cohort definition file, or to `all` cohorts.
 - The inclusion rule has to be valid according to [inclusion rule configuration](#inclusion-rule-configuration)
 
#### Inclusion Criteria Reference

Field | Example
trialId | ACTN 2021
referenceId | I-01
referenceText | Patient has to be 18 years old

The following checks are done on the level of inclusion criteria references:
 - Every trial references from a reference must be defined in the trial definition file.
 - Every referenceId must be unique within the context of a specific trial.

### Inclusion rule configuration

An inclusion rule has to be either a specific rule or a composite rule combining specific rules defined in [ACTIN-ALGO](../algo/README.md).
 - Inputs to composite rules are enclosed in parentheses
 - Inputs to specific rules are enclosed in brackets.

This framework allows the modeling of any kind of inclusion or exclusion criterion. Some examples:

Example | Rule
---|---
Patient has to be an adult | IS_AT_LEAST_18_YEARS_OLD
Has a maximum total bilirubin of 2.5 ULN, or 5.0 ULN in case patient has Gilbert's disease | OR(HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X[2.5], AND(HAS_GILBERT_DISEASE, HAS_DIRECT_BILIRUBIN_ULN_AT_MOST_X[5]))
Patient has no active CNS metastases | NOT(HAS_ACTIVE_CNS_METASTASES)

### Version History and Download Links
 - Upcoming (first release) 