## ACTIN-Clinical

ACTIN-Clinical ingests (external) clinical feed and uses an internal curation database to create data in terms of the datamodel described below.
This clinical model is written to a per-sample json file. The clinical data can be loaded into a mysql database via [ACTIN-Database](../database/README.md).

This application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.clinical.ClinicalIngestionApplication \
   -feed_directory /path/to/feed
   -curation_directory /path/to/curation
   -output_directory /path/to/where/clinical_json_files/are/written
```

### Disease Ontology ID

For mapping of primary tumor location and type, second primaries and 'other conditions' in the ACTIN clinical data model, 
one or more Disease Ontology IDs (DOIDs) are assigned. For more information, see https://disease-ontology.org/.

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
hasMeasurableDisease | 1 | Can patient's disease can be measured according to the typical method for the tumor type (eg RECIST for solid tumors, RANO for gliomas)?
hasBrainLesions | 0 | Patient has brain lesions?
hasActiveBrainLesions | NA | Patient has active (non-stable) brain lesions? (NA if hasBrainLesions = 0)
hasCnsLesions | 1 | Patient has central nervous system (CNS) lesions?
hasActiveCnsLesions | 1 | Patient has active (non-stable) CNS lesions? (NA if hasCnsLesions = 0)
hasBoneLesions | 0 | Patient has bone lesions?
hasLiverLesions | 1 | Patient has liver lesions?
hasLungLesions | 1 | Patient has lung lesions?
otherLesions | Lymph node, Abdominal | List of lesions not captured by explicit lesion fields (such as hasBoneLesions).
biopsyLocation | Liver | Lesion from which the biopsy for molecular analyses was obtained
 
1 clinical status

Field | Example Value | Details
---|---|---
who | 1 | Assigned WHO status of patient (0 to 5)
hasActiveInfection | 0 | Patient has active infection?
activeInfectionDescription | Lung abscess | Description of the active infection, inc ase hasActiveInfection = 1 
hasSigAberrationLatestECG | 1 | Patient had significant aberration on latest ECG?
ecgAberrationDescription | Atrial arrhythmia | Description of ECG aberration, in case hasSigAberrationLatestECG = 1
qtcfValue | NULL | Value of QTcF (QT corrected for heart rate using Fridericia's formula), in case it was described in ECG aberration
qtcfUnit | NULL | Unit of QTcF, in case it was described in ECG aberration
lvef | NULL | Left ventricle ejection fraction (LVEF)

N prior tumor treatments

Field | Example Value | Details
---|---|---
name | Ipilimumab | Treatment name
startYear | 2021 | Year in which treatment was started
startMonth | 11 | Month in which treatment was started 
stopYear | 2021 | Year in which treatment was stopped
stopMonth | 12 | Month in which treatment was stopped 
bestResponse | Complete response | Best response to treatment
stopReason | | Reason of treatment end
categories | Immunotherapy | A set of categories assigned to the treatment
isSystemic | 1 | Treatment is systemic?
chemoType | | Type of chemotherapy (if applicable)
immunoType | Anti-CTLA-4 | Type of immunotherapy (if applicable)
targetedType | | Type of targeted therapy (if applicable)
hormoneType | | Type of hormonal therapy (if applicable)
carTType | | Type of car-T therapy (if applicable)
transplantType | | Type of transplantation therapy (if applicable)
supportiveType | | Type of supportive treatment (if applicable)
trialAcronym | | Acronym of trial (if applicable)

N prior second primaries   

Field | Example Value | Details
---|---|---
tumorLocation | Bone/Soft tissue | Tumor location
tumorSubLocation | | Tumor sub location
tumorType | Schwannoma | Tumor type
tumorSubType | | Tumor sub type
doids | 3192 | Separated by ";"
diagnosedYear | 2018 | Year in which diagnosis of other tumor was made
diagnosedMonth | 10 | Month in which diagnosis of other tumor was made
treatmentHistory | Resection | Treatment history of the other primary tumor
isActive | 1 | Is the other primary tumor considered active?

N prior other conditions

Field | Example Value | Details
---|---|---
name | Pancreatitis | Other condition considered relevant for treatment decision making
year | 2020 | Year in which other condition was diagnosed
month | 8 | Month in which other condition was diagnosed
doids | 4989 | Separated by ";"
category | Pancreas disease | Assigned category of considered condition, based on DOIDs
isContraindicationForTherapy | 1 | Boolean if considered condition can have impact on therapy indication 

N prior (non-WGS) molecular tests

Field | Example Value | Details
---|---|---
test | IHC | Type of test
item | PD-L1 | Item measured
measure | CPS | Measure of test (if applicable)
scoreText | | Test score in text
scoreValuePrefix | > | Prefix for test score in value (if applicable)
scoreValue | 10 | Test score in value 
scoreValueUnit | | Unit for test score in value (if applicable)

N cancer related complications

Field | Example Value | Details
---|---|---
name | Uncontrolled pain | Complication considered relevant for treatment decision making
categories | Pain | Categories considered relevant for corresponding complication, separated by ";"
year | 2020 | Year in which complication occurred  
month | 7 | Month in which complication occurred  

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

N intolerances

Field | Example Value | Details
---|---|---
name | Pembrolizumab | Name of intolerance
doids | | if applicable
category | Medication | Category of intolerance
subcategories | Monoclonal antibody | Subcategory of category, when category = medication
type | Allergy | 'Allergy', 'Side effect' or 'Not specified'
clinicalStatus | Active | Clinical applicability
verificationStatus | Confirmed | Confirmation status
criticality | High | Assigned criticality of intolerance

N surgeries

Field | Example Value | Details
---|---|---
endDate | 2021-07-01 | Date on which surgery ended

N vital function measurements

Field | Example Value | Details
---|---|---
date | 2021-07-01 | Date of measurement
category | Non-invasive blood pressure | Category of the measurement 
subcategory | Systolic blood pressure | Subcategory of the measurement
value | 155 | 
unit | mm[Hg] | Unit of measurement

N body weight measurements

Field | Example Value | Details
---|---|---
date | 2021-07-01 | Date of measurement
value | 70 
unit | kilogram | Unit of measurement

N blood transfusions

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
status | ON_HOLD | Status of medication 

### Version History and Download Links
 - Upcoming (first release) 
