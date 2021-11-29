## ACTIN-SERVE-Bridge

ACTIN-SERVE-Bridge generates a [SERVE](https://github.com/hartwigmedical/hmftools/blob/master/serve/README.md) knowledgebase based on
the ACTIN treatments produced by [ACTIN-Treatment](../treatment/README.md). This allows SERVE to ingest molecular criteria from ACTIN 
treatments into its consolidated knowledgebase. 

```
java -cp actin.jar com.hartwig.actin.serve.ServeBridgeApplication \
    -treatment_database_directory /path/to/potential_treatment_options \
    -output_serve_knowledgebase_tsv /path/to/actin_knowledgebase.tsv
```

### Version History and Download Links
 - Upcoming (first release) 
 