## ACTIN-Database

ACTIN-Database allows loading the outputs of other ACTIN modules into a mysql database. 

Before ACTIN-Database can be used, a schema has to be set up on a mysql server (v5.7.21 to v8.0.18 are supported) from the database
schema defined in [generate_database.sql](src/main/resources/generate_database.sql). 

Currently ACTIN-Database can only load up the clinical output from [ACTIN-Clinical](../actin-clinical/README.md), as follows:

```
java -cp actin.jar com.hartwig.actin.database.ClinicalLoaderApplication \
    -clinical_model_json /path/to/clinical_model.json \
    -db_user user -db_pass pass -db_url url
```

## Version History and Download Links
 - Upcoming (first release) 
 