## ACTIN-Clinical

ACTIN-Clinical ingests (external) clinical feed and uses an internal curation database to create data in terms of the datamodel described below.
This clinical model is written to a single json file (see [ACTIN-Database](../actin-database/README.md) for how to load this model into a mysql database)

ACTIN-Clinical requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.clinical.ClinicalIngestionApplication \
   -feed_directory /path/to/feed
   -curation_directory /path/to/curation
   -json_output_file /path/to/clinical_model_output.json
```

## Clinical Datamodel

Every sample, uniquely defined by their sample ID, has a clinical record with the following data:

1 patient details

Field | Details
---|---
sex | Either MALE or FEMALE
birthYear | 
registrationDate | Date on which the patient was registered for evaluation in ACTIN
questionnaireDate | Date on which the data without timestamp has been collected (eg tumor location, WHO status)

1 tumor details

Field | Details
---|---
primaryTumorLocation | eg "Colorectum"
primaryTumorSubLocation | eg "Colon"
primaryTumorType | eg "Carcinoma"
primaryTumorSubType |eg "Neuroendocrine carcinoma
primaryTumorExtraDetails |
doids | separated by ";"
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
otherLesions | separated by ";"
biopsyLocation | location of the biopsy for which genomic analyses have been performed
 
1 clinical status

Field | Details
---|---
who | WHO status of patient (0 to 5)
hasActiveInfection |
hasSigAberrationLatestEcg |
ecgAberrationDescription |

N prior tumor treatments

Field | Details
---|---
name | eg "Capecitabine"
year |
category | eg "chemotherapy"
isSystemic |
chemoType | only filled in when applicable
immunoType | only filled in when applicable
targetedType | only filled in when applicable
hormoneType | only filled in when applicable
stemCellTransType | only filled in when applicable

N prior second primaries   

Field | Details
---|---
tumorLocation | eg "Colorectum"
tumorSubLocation | eg "Colon"
tumorType | eg "Carcinoma"
tumorSubType | eg "Neuroendocrine carcinoma"
doids | separated by ";"
diagnosedYear |
isSecondPrimaryActive |

N prior other conditions

Field | Details
---|---
name | eg "Endometriosis"
doids | separated by ";"
category | eg "Female reproductive system disease"

N cancer related complications

Field | Details
---|---
name | eg "Ascites"

N other complications (!! in-development)

Field | Details
---|---
name |
doids |
specialty |
onsetDate |
category |
status |

N lab values

Field | Details
---|---
date | 
code | eg "TBIL"
name | eg "Total bilirubin"
comparator | eg ">"
value |
unit | eg "umol/l"
refLimitLow |
refLimitUp |
isOutsideRef |

N toxicities

Field | Details
---|---
name | eg "Rash"
evaluatedDate |
source | either EHR or Questionnaire, depending on where the data originated from
grade |

N allergies

Field | Details
---|---
name | eg "Hay fever"
category | eg "environment"
criticality | eg "low"

N surgeries

Field | Details
---|---
endDate |

N blood pressures

Field | Details
---|---
date |
category | eg "Systolic blood pressure 
value |
unit | eg "mm\[Hg\]"

N blood transfusions (!! In development)

Field | Details
---|---
date |
product |

N medications

Field | Details
---|---
name | eg "Paracetamol"
type |
dosageMin | eg "2000"
dosageMax | eg "3000"
unit | eg "mg"
frequencyUnit | eg "day"
ifNeeded | Determines whether the medication is mandatory or only "if needed".
startDate |
stopDate |
active | Determines whether the medication is currently being administered.