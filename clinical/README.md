## ACTIN-Clinical

ACTIN-Clinical ingests (external) clinical feed and uses an internal curation database to create data in terms of the datamodel described below.
This clinical model is written to a per-sample json file. 
See [Database](../database/README.md) for how to load this model into a mysql database.

ACTIN-Clinical requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.clinical.ClinicalIngestionApplication \
   -feed_directory /path/to/feed
   -curation_directory /path/to/curation
   -output_directory /path/to/where/clinical/data/is/written
```
### Disease Ontology ID
For mapping of primary tumor location and type, second primaries and 'other conditions' in the ACTIN clinical data model, one or more Disease Ontology IDs (DOIDs) are assigned. For more information, see https://disease-ontology.org/.

## Clinical Datamodel

Every sample, uniquely defined by their sample ID, has a clinical record with the following data:

1 patient details

Field | Example Value | Details
---|---|---
gender | MALE | 
birthYear | 1950 |
registrationDate | 2021-07-11 | Date on which the patient was registered for evaluation in ACTIN
questionnaireDate | 2021-07-20 | Date on which the data without EHR timestamp has been collected (eg lesion locations, WHO status)

1 tumor details

Field | Example Value | Details
---|---|---
primaryTumorLocation | Skin | Tumor location
primaryTumorSubLocation | | Tumor sub location
primaryTumorType | Melanoma | Tumor type
primaryTumorSubType | | Tumor sub type
primaryTumorExtraDetails | | Additional tumor information that cannot be captured in previous fields 
doids | 8923 | Separated by ";"
stage | IV | Tumor stage grouping. Roman numeral from I to IV with further subdivision with letters (A, B or C) if available, eg IIIA
hasMeasurableLesionRecist | 1 | Patient has at least one lesion that can be measured using RECIST criteria?
hasBrainLesions | 0 | Patient has brain lesions?
hasActiveBrainLesions | NA | Patient has active (non-stable) brain lesions? (NA if hasBrainLesions = 0)
hasSymptomaticBrainLesions | NA | Patient has symptomatic brain lesions? (NA if hasBrainLesions = 0)
hasCnsLesions | 1 | Patient has central nervous system (CNS) lesions?
hasActiveCnsLesions | 1 | Patient has active (non-stable) CNS lesions? (NA if hasCnsLesions = 0)
hasSymptomaticCnsLesions | 0 | Patient has symptomatic CNS lesions? (NA if hasCnsLesions = 0)
hasBoneLesions | 0 | Patient has bone lesions?
hasLiverLesions | 1 | Patient has liver lesions?
hasOtherLesions | 1 | Patient has lesions that are not captured in hasBrainLesions, hasCnsLesions, hasBoneLesions and hasLiverLesions?
otherLesions | Pulmonal, Abdominal | Description of other lesions, in case hasOtherLesions = 1
biopsyLocation | Liver | Lesion from which the biopsy for genomic analyses was obtained
 
1 clinical status

Field | Example Value | Details
---|---|---
who | 1 | Assigned WHO status of patient (0 to 5)
hasActiveInfection | 0 | Patient has active infection?
hasSigAberrationLatestEcg | 1 | Patient had significant aberration on latest ECG?
ecgAberrationDescription | Atrial arrhythmia | Description of ECG aberration, in case hasSigAberrationLatestEcg = 1

N prior tumor treatments

Field | Example Value | Details
---|---|---
name | Ipilimumab | Treatment name
year | 2021 | Year in which treatment was given
category | Immunotherapy | Type of treatment
isSystemic | 1 | Treatment is systemic?
chemoType | | Type of chemotherapy (if applicable)
immunoType | Anti-CTLA-4 | Type of immunotherapy (if applicable)
targetedType | | Type of targeted therapy (if applicable)
hormoneType | | Type of hormonal therapy (if applicable)
stemCellTransType | | Type of stem cell transplantation therapy (if applicable)

N prior second primaries   

Field | Example Value | Details
---|---|---
tumorLocation | Bone/Soft tissue | Tumor location
tumorSubLocation | | Tumor sub location
tumorType | Schwannoma | Tumor type
tumorSubType | | Tumor sub type
doids | 3192 | Separated by ";"
diagnosedYear | 2018 | Year in which diagnosis of other tumor was made
treatmentHistory | Resection | Treatment history of the other primary tumor
isActive | 1 | Is the other primary tumor considered active?

N prior other conditions

Field | Example Value | Details
---|---|---
name | Pancreatitis | Other condition considered relevant for treatment decision making
doids | 4989 | Separated by ";"
category | Pancreas disease | Assigned category of considered condition, based on DOIDs

N cancer related complications

Field | Example Value | Details
---|---|---
name | Ascites | Cancer related complication considered relevant for treatment decision making

N other complications (!! in-development)

Field | Example Value | Details
---|---|---
name |
doids |
specialty |
onsetDate |
category |
status |

N lab values

Field | Example Value | Details
---|---|---
date | 2021-07-01 | Date on which lab value was measured
code | Hb | Code of lab value
name | Hemoglobin | Name/description of lab value
comparator | | ">" or "<", if applicable
value | 5.5 |
unit | mmol/L | Lab value unit
refLimitLow | 6.5 | Considered normal range lower limit
refLimitUp | 9.5 | Considered normal range upper limit
isOutsideRef | 1 | Measured value is outside normal range?

N toxicities

Field | Example Value | Details
---|---|---
name | Fatigue | Name of measured toxicity
evaluatedDate | 2021-07-01 | Date on which toxicity was measured
source | Questionnaire | EHR or Questionnaire, depending on where the toxicity data originated from
grade | 2 | Determined grade of toxicity

N allergies

Field | Example Value | Details
---|---|---
name | Pembrolizumab | Allergy
category | Medication | Category of allergy
criticality | High | Assigned criticality of allergy

N surgeries

Field | Example Value | Details
---|---|---
endDate | 2021-07-01 | Date on which surgery ended

N blood pressures

Field | Example Value | Details
---|---|---
date | 2021-07-01 | Date on which blood pressure value was measured
category | Systolic blood pressure | Mean, systolic or diastolic blood pressure
value | 155 | 
unit | mm[Hg] | Unit in which blood pressure was measured

N blood transfusions (!! In development)

Field | Example Value | Details
---|---|---
date | 2021-07-01 | Date on which blood transfusion was given
product | Thrombocyte concentrate | Blood product of transfusion

N medications

Field | Example Value | Details
---|---|---
name | Ibuprofen | Medication name
type | NSAIDs | Type of medication
dosageMin | 750 | Assigned minimal dosage
dosageMax | 1000 | Assigned maximal dosage (dosageMin and dosageMax can be equal)
dosageUnit | mg | Dosage unit
frequency | 1 | Assigned frequency of dosage
frequencyUnit | day | Frequency unit
ifNeeded | 0 | Determines whether the medication should be taken according to dosage prescription or only "if needed"
startDate | 2021-07-01 | Assigned start date of medication
stopDate | 2021-10-01 | Assigned stop date of medication (if applicable)
active | 1 | Medication is prescribed at time of latest EHR evaluation?

### Version History and Download Links
 - Upcoming (first release) 
