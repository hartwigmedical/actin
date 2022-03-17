## ACTIN-Database

ACTIN-Database allows loading the outputs of other ACTIN modules into a mysql database. 

Before this application can be used, a schema has to be set up on a mysql server (v5.7.21 to v8.0.18 are supported) from the database
schema defined in [generate_database.sql](src/main/resources/generate_database.sql). See [readme](src/main/resources/readme) for help on 
additional steps and what is required to be able to compile this module.

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

### Treatment Data Loader
The treatment data loader loads up treatment data from [ACTIN-Treatment](../treatment/README.md) as follows:

```
java -cp actin.jar com.hartwig.actin.database.treatment.TreatmentLoaderApplication \
    -treatment_database_directory /path/to/treatment_database_dir \
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
 