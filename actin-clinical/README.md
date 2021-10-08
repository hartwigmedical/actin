## ACTIN-Clinical

ACTIN-Clinical ingests (external) clinical feed and uses an internal curation database to create data in terms of the datamodel described below.
This clinical model is written to a single json file (see [database](../actin-database/README.md) for how to load this model into a mysql database)

ACTIN-Clinical requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.clinical.ClinicalIngestionApplication \
   -feed_directory /path/to/feed
   -curation_directory /path/to/curation
   -json_output_file /path/to/clinical_model_output.json
```

## Clinical Datamodel

Every sample has a clinical record with the following data:

1 patient details

Field | Details
---|---
sex | Either MALE or FEMALE
birthYear | 
registrationDate | Date on which the patient was registered for evaluation in ACTIN
questionnaireDate | Date on which the data without timestamp has been collected (eg tumor location)

1 tumor details

Field | Details
---|---
primaryTumorLocation | eg "Colorectum"
primaryTumorSubLocation | eg "Colon"
primaryTumorType | eg "Carcinoma"
primaryTumorSubType |eg "Neuroendocrine carcinoma
primaryTumorExtraDetails |
doids | List of tumor doids (separated by ";")
stage | Tumor stage (either 'I', 'II', 'III' or 'IV')
hasMeasurableLesionRecist | patient has at least one lesion on which RECIST measurements can be done
hasBrainLesions |
hasActiveBrainLesions |
hasSymptomaticBrainLesions |
hasCnsLesions | cns = central nervous system
hasActiveCnsLesions |
hasSymptomaticCnsLesions |
hasBoneLesions |
hasLiverLesions |
hasOtherLesions | 
otherLesions | Separated by ";"
biopsyLocation | location of the biopsy for which genomic analyses have been performed
 
1 clinical status
 
 
N prior tumor treatments

N prior  