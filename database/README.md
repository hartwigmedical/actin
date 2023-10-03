## ACTIN-Database

ACTIN-Database allows loading the outputs of other ACTIN modules into a mysql database.

A database is expected to be present on a mysql server (v5.7.21 to v8.0.18 are supported) generated from the database
schema defined in [generate_database.sql](src/main/resources/generate_database.sql).

### Clinical Data Loader
The clinical data loader loads up clinical data from [ACTIN-Clinical](../clinical/README.md) as follows:

```
java -cp actin.jar com.hartwig.actin.database.clinical.ClinicalLoaderApplication \
    -clinical_directory /path/to/clinical_json_files \
    -db_user user -db_pass pass -db_url url
```

### Molecular Data Loader

The molecular data loader loads up molecular data from [ACTIN-Molecular](../molecular/README.md) as follows:

```
java -cp actin.jar com.hartwig.actin.database.molecular.MolecularLoaderApplication \
    -molecular_json /path/to/molecular_json \
    -db_user user -db_pass pass -db_url url
```

### Trial Data Loader

The trial data loader loads up trial data from [ACTIN-Trial](../trial/README.md) as follows:

```
java -cp actin.jar com.hartwig.actin.database.trial.TrialLoaderApplication \
    -trial_database_directory /path/to/trial_database_dir \
    -db_user user -db_pass pass -db_url url
```

### Treatment Match Data Loader

The treatment match data loader loads up treatment match results from [ACTIN-Algo](../algo/README.md) as follows:

```
java -cp actin.jar com.hartwig.actin.database.algo.TreatmentMatchLoaderApplication \
    -treatment_match_json /path/to/treatment_match_json \
    -db_user user -db_pass pass -db_url url
```

### Version History and Download Links
 - Upcoming (first release) 
 