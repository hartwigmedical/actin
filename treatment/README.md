## ACTIN-Treatment

ACTIN-Treatment creates a database of potential treatments that [ACTIN-Algo](../algo/README.md) matches against.  
The application takes in a set of configuration files and writes a database in JSON format to a specified output directory. 

This application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.treatment.TreatmentCreatorApplication \
   -trial_config_directory /path/to/trial_config_dir
   -output_directory /path/to/where/treatment_json_files/are/written
```

### Configuration of the treatment database

Coming soon

### Version History and Download Links
 - Upcoming (first release) 