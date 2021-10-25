## ACTIN-Treatment

ACTIN-Treatment creates a database of potential treatments that [ACTIN-algo](../algo/README.md) matches against.  
The application takes in a set of configuration files and writes a database in JSON format to a specified output directory. 

ACTIN-Treatment requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.treatment.TreatmentCreatorApplication \
   -trial_config_directory /path/to/trial_config
   -output_directory /path/to/where/treatment_database/is/written
```

### Configuration of the treatment database

Coming soon

### Version History and Download Links
 - Upcoming (first release) 