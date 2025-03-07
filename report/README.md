## ACTIN-Report

ACTIN-Report generates a PDF based on all data fed into ACTIN and generated by [ACTIN-Algo](../algo/README.md).
An example PDF can be found [here](src/main/resources/example.actin.pdf)

This application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.report.ReporterApplication \
   -clinical_json /path/to/clinical.json \
   -molecular_json /path/to/molecular.json \
   -treatment_match_json /path/to/treatment_match.json \
   -output_directory /path/where/pdf/is/written \
```

The following assumptions are made about the inputs:
 - The clinical JSON adheres to the format that is generated by [ACTIN-Clinical](../clinical/README.md).
 - The molecular JSON adheres to the format that is generated by [ACTIN-Molecular](../molecular/README.md)
 - The treatment match JSON is the output generated by [ACTIN-Algo](../algo/README.md)
 
Optionally, the flag `-enable_extended_mode` can be provided to include the full trial matching details.
 
### Version History and Download Links
 - Upcoming (first release) 
