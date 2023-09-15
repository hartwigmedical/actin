## ACTIN-Clinical

ACTIN-Clinical ingests (external) clinical feed and uses an internal curation database to create data in terms of the ACTIN datamodel.
Clinical data in the ACTIN datamodel is written to a per-patient json file. The clinical data can be loaded into a mysql database
via [ACTIN-Database](../database/README.md).

This application requires Java 11+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.clinical.ClinicalIngestionApplicationKt \
   -feed_directory /path/to/feed_file_dir \
   -curation_directory /path/to/curation_file_dir \
   -doid_json /path/to/full_doid_tree_json_file \
   -output_directory /path/to/where/clinical_json_files/are/written
```

## External clinical feed

### Required set

The (external) clinical feed is submitted to ACTIN using below datamodel, for every patient.

The column 'with date?' indicates whether the variable should be provided with corresponding date. Note that all variables with 'with date?
= Yes' can be provided N times.

Patient details

| Variable                | Example values | With date? |
|-------------------------|----------------|------------|
| Birth year              | 1940           |            |
| Sex                     | Male           |            |    
| Gender                  | Male           |            |
| ACTIN registration date | 2023-01-01     | N/A        |
| WHO                     | 0/1/2/3/4/5    | Yes        |

Current primary tumor details

| Variable                            | Example values               | With date? |
|-------------------------------------|------------------------------|------------|
| Diagnosis date                      | 2023-01-01                   | N/A        |
| Tumor localization details          | Lung                         |            |
| Tumor type details                  | Adenocarcinoma               |            |    
| Tumor grade/differentiation details | Poorly differentiated        |            |
| Tumor stage                         | 4 / T4N1M0                   | Yes        |
| Lesion site + active*?              | Liver / Bone / Brain, active | Yes        |
| Measurable disease?                 | Yes                          | Yes        |

*: Only applicable in case of CNS or brain lesions

Treatment history current tumor (N records per patient)

| Variable                                       | Example values          | With date? |
|------------------------------------------------|-------------------------|------------|
| Name                                           | Gemcitabine+Cisplatin   |            |
| Intention                                      | Palliative              |            |
| Start date                                     | 2023-02-01              | N/A        |    
| End date                                       | 2023-10-01              | N/A        |
| Stop reason                                    | Progressive disease     | Yes        |
| Response                                       | Partial response        | Yes        |
| Intended number of cycles                      | 6                       |            |
| Administered number of cycles                  | 6                       |            |
| Modifications to treatment composition         | Gemcitabine+Carboplatin | Yes        |
| Nr of cycles after which modification occurred | 3                       |            |
| Occurrences of grade => 2 toxicities           | Neuropathy              | Yes        |
| Treatment administered in clinical study?      | Yes / No                |            |

Note that only fields relevant for that type of treatment need to be provided. E.g. for surgeries, only name, intention and date need to be
provided.
Finally, treatment name should be as detailed as possible (e.g. 'Gemcitabine+Cisplatin' is preferred over 'Chemotherapy')

Molecular test history current tumor (N records per patient)

| Variable                                                    | Example values | With date? |
|-------------------------------------------------------------|----------------|------------|
| Type                                                        | IHC            |            |
| Measure (i.e. gene or protein)                              | HER2           |            |
| Result                                                      | Negative / 3+  | Yes        |
| Biopsy location (of biopsy analyzed in test), if applicable | Liver          | Yes        |

Note: For WGS/NGS data, the BAM (raw data) should be provided rather than above format.

Other relevant patient history: previous primary tumor(s) (N records per patient)

| Variable                   | Example values | With date? |
|----------------------------|----------------|------------|
| Diagnosis date             | 1999-01-01     | N/A        |
| Tumor localization details | Colon          |            |
| Tumor type details         | Carcinoma      |            |
| Treatment history: name(s) | Laparoscopy    | Yes        |
| Status details             | Inactive       | Yes        |

Other relevant patient history: other (N records per patient)

| Variable                 | Example values | With date? |
|--------------------------|----------------|------------|
| Name                     | Pancreatitis   |            |
| Start date               | 1999-01-01     | N/A        |
| End date (if applicable) |                | N/A        |

Cancer related complications (N records per patient)

| Variable                 | Example values | With date? |
|--------------------------|----------------|------------|
| Name                     | Ascites        |            |
| Start date               | 1999-01-01     | N/A        |
| End date (if applicable) |                | N/A        |

Toxicities (N records per patient)

| Variable | Example values | With date? |
|----------|----------------|------------|
| Name     | Neuropathy     |            |
| Grade    | 2              | Yes        |

Note that in case a toxicity is established without a grade assigned, the date on which the toxicity is assigned should be the date
of 'grade' (and grade can be null).

Medication details (N records per patient)

| Variable                                | Example values | With date? |
|-----------------------------------------|----------------|------------|
| Drug name                               | Paracetamol    |            |
| ATC code                                | N02BE01        |            |
| Start date                              | 2023-03-01     | N/A        |
| End date                                | 2023-07-01     | N/A        |
| Administration route                    | Oral           |            |
| Dosage                                  | 500            |            |
| Dosage unit                             | mg             |            |
| Frequency                               | 2              |            |
| Frequency unit                          | day            |            |
| Period between dosages value            | 1              |            |
| Period between dosages unit             | day            |            |
| Administration only if needed? (Yes/No) | Yes            |            |

Note: Information about ATC codes can be found at the website of WHOCC: https://www.whocc.no/atc_ddd_index/

Lab details (N records per patient)

| Variable                            | Example values           | With date? |
|-------------------------------------|--------------------------|------------|
| Measure                             | Carcinoembryonic antigen |            |
| Comparator (if applicable)          | >                        |            |
| Value                               | 3.5                      | Yes        |
| Unit                                | ug/L                     |            |
| Institutional lower reference limit |                          |            |
| Institutional upper reference limit |                          |            |

Blood transfusion details (N records per patient)

| Variable | Example values          | With date? |
|----------|-------------------------|------------|
| Product  | Thrombocyte concentrate | Yes        |

Vital function details (N records per patient)

| Variable | Example values          | With date? |
|----------|-------------------------|------------|
| Measure  | Systolic blood pressure |            |
| Value    | 2                       | Yes        |
| Unit     |                         |            |

Vital function measures of interest include: blood pressure (systolic, diastolic), pulse oximetry, heart rate, BMI, body weight

ECG details (N records per patient)

| Variable                   | Example values    | With date? |
|----------------------------|-------------------|------------|
| Aberration (if applicable) | Atrial arrhythmia | Yes        |
| qtcf value (if measured)   |                   | Yes        |
| qtcf unit (if measured)    |                   |            |
| lvef value (if measured)   |                   |            |
| lvef unit (if measured)    |                   |            |

Allergy details (N records per patient)

| Variable | Example values                        | With date? |
|----------|---------------------------------------|------------|
| Name     | Pembrolizumab                         | Yes        |
| Type     | Allergy, Side effect or Not specified |            |

Infection details (N records per patient)

| Variable                    | Example values | With date? |
|-----------------------------|----------------|------------|
| Start date                  | 2023-06-01     | N/A        |
| End date  (if applicable)   |                | N/A        |
| Description (if applicable) |                |            |

### Additional (optional) set

Below is a set of variables that is not necessarily required to run ACTIN, but if the variables are available, the variables in this set can
be mapped to the ACTIN clinical model as well:

| Category   | Variable              | Example values       | With date? |
|------------|-----------------------|----------------------|------------|
| N/A        | Date of questionnaire | 2023-01-01           | N/A        |
| Toxicities | Source of data        | EHR or questionnaire |            |
| Allergies  | clinical status       | Active               |            |
| Allergies  | verificationStatus    | Confirmed            |            |
| Allergies  | criticality           | High                 |            |
| Surgeries  | Date                  | 2023-01-01           |            |
| Surgeries  | Status                | Finished             |            |

## ACTIN clinical datamodel

In ACTIN, the clinical feed as described above, is mapped onto the ACTIN clinical data model.

### Disease Ontology ID

For mapping of primary tumor location and type, second primaries and other conditions in the ACTIN clinical datamodel,
one or more Disease Ontology IDs (DOIDs) are assigned. For more information, see https://disease-ontology.org/.

### Datamodel

The fields of the ACTIN clinical datamodel are described below. "Origin" indicates where the data of this field originates from.

Note that "if applicable" in 'origin' indicates that the field is derived from a variable of the 'optional' set.

1 patient details

| Field             | Origin                           |
|-------------------|----------------------------------|
| birthYear         | Patient: Birth year              |
| gender            | Patient: Gender                  |
| registrationDate  | Patient: ACTIN registration date |
| questionnaireDate | If applicable                    |

1 tumor details

| Field                    | Origin                                               |
|--------------------------|------------------------------------------------------|
| primaryTumorLocation     | Primary tumor details: Tumor localization            |
| primaryTumorSubLocation  | Primary tumor details: Tumor localization            |
| primaryTumorType         | Primary tumor details: Tumor type                    |
| primaryTumorSubType      | Primary tumor details: Tumor type                    |
| primaryTumorExtraDetails | Primary tumor details: Grade/differentiation details |
| doids                    | Added in curation                                    |
| stage                    | Primary tumor details: Stage                         |
| hasMeasurableDisease     | Primary tumor details: Measurable disease?           |
| hasBrainLesions          | Primary tumor details: Lesion sites                  |
| hasActiveBrainLesions    | Primary tumor details: Lesion sites                  |
| hasCnsLesions            | Primary tumor details: Lesion sites                  |
| hasActiveCnsLesions      | Primary tumor details: Lesion sites                  |
| hasBoneLesions           | Primary tumor details: Lesion sites                  |
| hasLiverLesions          | Primary tumor details: Lesion sites                  |
| hasLungLesions           | Primary tumor details: Lesion sites                  |
| otherLesions             | Primary tumor details: Lesion sites                  |
| biopsyLocation           | Molecular test details: Biopsy location              |

1 clinical status

| Field                      | Origin                         |
|----------------------------|--------------------------------|
| who                        | Patient: WHO                   |
| hasActiveInfection         | Infection details: Date        |
| activeInfectionDescription | Infection details: Description |
| hasToxicitiesGrade2        | Toxicity details               |
| hasSigAberrationLatestECG  | ECG details                    |
| ecgAberrationDescription   | ECG details                    |
| qtcfValue                  | ECG details                    |
| qtcfUnit                   | ECG details                    |
| lvef                       | ECG details                    |
| hasComplications           | Complication details           |

N prior tumor treatments (TO BE UPDATED)

| Field          | Origin                                          |
|----------------|-------------------------------------------------|
| name           | Treatment history name                          |
| startYear      | Treatment history start date                    |
| startMonth     | Treatment history start date                    |
| stopYear       | Treatment history end date                      |
| stopMonth      | Treatment history end date                      |
| cycles         | Treatment history administered number of cycles |
| bestResponse   | Treatment history response                      |
| stopReason     | Treatment history stop reason                   |
| categories     | Added in curation                               |
| isSystemic     | Added in curation                               |

N prior second primaries

| Field            | Origin                                     |
|------------------|--------------------------------------------|
| tumorLocation    | Previous primary tumors: Tumor location    |
| tumorSubLocation | Previous primary tumors: Tumor location    |
| tumorType        | Previous primary tumors: Tumor type        |
| tumorSubType     | Previous primary tumors: Tumor type        |
| doids            | Added in curation                          |
| diagnosedYear    | Previous primary tumors: Diagnosis date    |
| diagnosedMonth   | Previous primary tumors: Diagnosis date    |
| treatmentHistory | Previous primary tumors: Treatment history |
| status           | Previous primary tumors: Status            |

N prior other conditions

| Field                        | Origin                      |
|------------------------------|-----------------------------|
| name                         | Other condition: name       |
| year                         | Other condition: start date |
| month                        | Other condition: start date |
| doids                        | Added in curation           |
| category                     | Added in curation           |
| isContraindicationForTherapy | Added in curation           |

N prior (non-WGS) molecular tests

| Field                                    | Origin                    |
|------------------------------------------|---------------------------|
| test                                     | Molecular test: Test type |
| item                                     | Molecular test: Name      |
| measure                                  | Molecular test: Result    |
| scoreText                                | Molecular test: Result    |
| scoreValuePrefix                         | Molecular test: Result    |
| scoreValue                               | Molecular test: Result    |
| scoreValueUnit                           | Molecular test: Result    |
| impliesPotentialPriorIndeterminateStatus | Added in curation         |

N cancer related complications

| Field      | Origin                   |
|------------|--------------------------|
| name       | Complication: name       |
| categories | Added in curation        |
| year       | Complication: start date |
| month      | Complication: start date |

N lab values

| Field        | Origin                                |
|--------------|---------------------------------------|
| date         | Lab values: measured date             |
| code         | Added in curation                     |
| name         | Lab values: Name                      |
| comparator   | Lab values: Comparator                |
| value        | Lab values: Value                     |
| unit         | Lab values: Unit                      |
| refLimitLow  | Lab values: Institutional lower limit |
| refLimitUp   | Lab values: Institutional upper limit |
| isOutsideRef | Added in curation                     |

N toxicities

| Field         | Origin            |
|---------------|-------------------|
| name          | Toxicities: Name  |
| evaluatedDate | Toxicities: Date  |
| grade         | Toxicities: Grade |
| source        | If applicable     |

N intolerances

| Field              | Details           |
|--------------------|-------------------|
| name               | Allergies: Name   |
| doids              | Added in curation |
| category           | Added in curation |
| subcategories      | Added in curation |
| type               | Allergies: Type   |
| clinicalStatus     | If applicable     |
| verificationStatus | If applicable     |
| criticality        | If applicable     |

N surgeries

| Field   | Origin        |
|---------|---------------|
| endDate | If applicable |
| status  | If applicable |

N vital function measurements

| Field       | Origin                |
|-------------|-----------------------|
| date        | Vital function: Date  |
| category    | Vital function: Name  |
| subcategory | Vital function: Name  |
| value       | Vital function: Value |
| unit        | Vital function: Unit  |

N body weight measurements

| Field | Origin                                              |
|-------|-----------------------------------------------------|
| date  | Vital function -> Body weight: Date of measurement  |
| value | Vital function -> Body weight: Value of measurement |
| unit  | Vital function -> Body weight: Unit of measurement  |

N blood transfusions

| Field   | Origin                     |
|---------|----------------------------|
| date    | Blood transfusion: Date    |
| product | Blood transfusion: Product |

N medications

| Field                            | Example Value                                                     | Origin                                   |
|----------------------------------|-------------------------------------------------------------------|------------------------------------------|
| name                             | Ibuprofen                                                         | Medication: name                         | 
| administrationRoute              | Oral                                                              | Medication: Administration route         |
| status                           | ON_HOLD                                                           | Added in curation                        |
| dosageMin                        | 750                                                               | Medication: min dosage                   |
| dosageMax                        | 1000                                                              | Medication: max dosage                   |
| dosageUnit                       | mg                                                                | Medication: Dosage unit                  |
| frequency                        | 1                                                                 | Medication: Dosage frequency             |
| frequencyUnit                    | day                                                               | Medication: Dosage frequency unit        |
| periodBetweenValue               | Months                                                            | Medication: Period between dosages value |
| periodBetweenUnit                | 2                                                                 | Medication: PPeriod between dosages unit |
| ifNeeded                         | 0                                                                 | Medication: If needed                    |
| startDate                        | 2021-07-01                                                        | Medication: Start date                   |
| stopDate                         | 2021-10-01                                                        | Medication: Stop date                    |
| cypInteractions.type             | 'Inducer', 'Inhibitor' or 'Substrate'                             | Added in curation                        |
| cypInteractions.strength         | 'Strong', 'Moderate', 'Weak', 'Sensitive' or 'Moderate sensitive' | Added in curation                        |
| cypInteractions.cyp              | CYP type                                                          | Added in curation                        |
| atc.anatomicalMainGroup.code     | N                                                                 | Optional/added in curation               |
| atc.anatomicalMainGroup.name     | NERVOUS SYSTEM                                                    | Optional/added in curation               |
| atc.therapeuticSubGroup.code     | N02                                                               | Optional/added in curation               |
| atc.therapeuticSubGroup.name     | ANALGESICS                                                        | Optional/added in curation               |
| atc.pharmacologicalSubGroup.code | N02A                                                              | Optional/added in curation               |
| atc.pharmacologicalSubGroup.name | OPIOIDS                                                           | Optional/added in curation               |
| atc.chemicalSubGroup.code        | N02AJ                                                             | Optional/added in curation               |
| atc.chemicalSubGroup.name        | Opioids in combination with non-opioid analgesics                 | Optional/added in curation               |
| atc.chemicalSubstance.code       | N02AJ08                                                           | Optional/added in curation               |
| atc.chemicalSubstance.name       | codeine and ibuprofen 	                                           | Optional/added in curation               |
| qtProlongatingRisk               | NONE 	                                                            | Added in curation                        |

### Version History and Download Links

- Upcoming (first release) 
