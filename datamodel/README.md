## ACTIN Datamodel

The ACTIN datamodel module defines the datamodel which ACTIN accepts as inputs and uses internally. 

## ACTIN clinical datamodel

### Disease Ontology ID

For primary tumor location and type, and second primaries in the ACTIN clinical datamodel, one or more Disease Ontology IDs (DOIDs) are
assigned. For more information, see https://disease-ontology.org/.

### International Classification of Diseases version 11 (ICD-11)

For other conditions, intolerances, toxicities and ECG data in the ACTIN clinical data model, one or more ICD-11 codes are
assigned. For more information, see https://icd.who.int/. To browse codes: https://icd.who.int/browse/2024-01/mms/en

### Datamodel

The fields of the ACTIN clinical datamodel are described below. "Origin" indicates where the data of this field originates from.

Note that "if applicable" in 'origin' indicates that the field is derived from a variable of the 'optional' set.

#### 1 patient details

| Field             | Origin                           |
|-------------------|----------------------------------|
| birthYear         | Patient: Birth year              |
| gender            | Patient: Gender                  |
| registrationDate  | Patient: ACTIN registration date |
| questionnaireDate | If applicable                    |

#### 1 tumor details

| Field                    | Origin                                     |
|--------------------------|--------------------------------------------|
| name                     | Description of tumor, added in curation    |
| doids                    | Added in curation                          |
| stage                    | Primary tumor details: Stage               |
| hasMeasurableDisease     | Primary tumor details: Measurable disease? |
| hasBrainLesions          | Primary tumor details: Lesion sites        |
| hasActiveBrainLesions    | Primary tumor details: Lesion sites        |
| hasCnsLesions            | Primary tumor details: Lesion sites        |
| hasActiveCnsLesions      | Primary tumor details: Lesion sites        |
| hasBoneLesions           | Primary tumor details: Lesion sites        |
| hasLiverLesions          | Primary tumor details: Lesion sites        |
| hasLungLesions           | Primary tumor details: Lesion sites        |
| otherLesions             | Primary tumor details: Lesion sites        |
| biopsyLocation           | Molecular test details: Biopsy location    |

#### 1 clinical status

| Field                      | Origin                         |
|----------------------------|--------------------------------|
| who                        | Patient: WHO                   |
| hasActiveInfection         | Other relevant patient history |
| activeInfectionDescription | Other relevant patient history |
| lvef                       | Other relevant patient history |
| hasOtherConditions         | Other condition details        |

#### 0+ treatment history entries in oncological history

| Field                   | Origin                       |
|-------------------------|------------------------------|
| treatments              | Described below              |
| startYear               | Treatment history start date |
| startMonth              | Treatment history start date |
| intents                 | Treatment history intent     |
| isTrial                 | Treatment history entry      |
| trialAcronym            | Treatment history entry      |
| treatmentHistoryDetails | Described below              |

Each treatment history entry references T treatments from ACTIN's treatment database:

| Field      | Origin               |
|------------|----------------------|
| name       | Resolved in curation |
| categories | Treatment database   |
| types      | Treatment database   |
| synonyms   | Treatment database   |
| isSystemic | Treatment database   |

Drug treatments inherit their categories and types from a curated set of named drugs, also maintained in ACTIN's treatment database.
Radiotherapy treatments always have RADIOTHERAPY as their sole category and may also indicate if treatment was internal.

Each treatment history entry has 0 or 1 treatment history details records:

| Field                  | Origin                      |
|------------------------|-----------------------------|
| stopYear               | Treatment history stop date |
| stopMonth              | Treatment history stop date |
| ongoingAsOf            | Not set - TODO: ACTIN-463   |
| cycles                 | Added in curation           |
| bestResponse           | Added in curation           |
| stopReason             | Added in curation           |
| stopReasonDetail       | Added in curation           |
| toxicities             | Added in curation           |
| bodyLocationCategories | Added in curation           |
| bodyLocations          | Added in curation           |
| switchToTreatments     | Described below             |
| maintenanceTreatment   | Described below             |

The details may include multiple treatment stages representing switches from the original treatment plan or a maintenance treatment:

| Field      | Origin                   |
|------------|--------------------------|
| treatment  | ACTIN treatment database |
| cycles     | Added in curation        |
| startYear  | Added in curation        |
| startMonth | Added in curation        |

#### 0+ prior second primaries

| Field            | Origin                                     |
|------------------|--------------------------------------------|
| name             | Added in curation                          |
| doids            | Added in curation                          |
| diagnosedYear    | Previous primary tumors: Diagnosis date    |
| diagnosedMonth   | Previous primary tumors: Diagnosis date    |
| treatmentHistory | Previous primary tumors: Treatment history |
| status           | Previous primary tumors: Status            |

#### 0+ comorbidities

Comorbidities are diseases or medical conditions that coexist alongside the primary diagnosis.
These consist of intolerances, toxicities, ECGs, and other conditions.
Each subtype has fields for name, year, month, and ICD codes to enable generic matching across all comorbidities.
They can also be considered individually:

##### 0+ other conditions

| Field    | Origin                      |
|----------|-----------------------------|
| name     | Other condition: name       |
| year     | Other condition: start date |
| month    | Other condition: start date |
| icdCodes | Added in curation           |

##### 0+ toxicities

| Field         | Origin            |
|---------------|-------------------|
| name          | Toxicities: Name  |
| evaluatedDate | Toxicities: Date  |
| icdCodes      | Added in curation |
| grade         | Toxicities: Grade |
| source        | If applicable     |
| endDate       | If provided       |

##### 0+ intolerances

| Field              | Details           |
|--------------------|-------------------|
| name               | Allergies: Name   |
| icdCodes           | Added in curation |
| type               | Allergies: Type   |
| clinicalStatus     | If applicable     |
| verificationStatus | If applicable     |
| criticality        | If applicable     |

##### 0+ ECGs

| Field       | Origin            |
|-------------|-------------------|
| name        | ECG details       |
| qtcfMeasure | ECG details       |
| jtcMeasure  | ECG details       |
| icdCodes    | Added in curation |

#### 0+ prior (non-WGS) molecular tests

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

#### 0+ lab values

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

#### 0+ surgeries

| Field   | Origin        |
|---------|---------------|
| endDate | If applicable |
| status  | If applicable |

#### 0+ vital function measurements

| Field       | Origin                |
|-------------|-----------------------|
| date        | Vital function: Date  |
| category    | Vital function: Name  |
| subcategory | Vital function: Name  |
| value       | Vital function: Value |
| unit        | Vital function: Unit  |

#### 0+ body weight measurements

| Field | Origin                                              |
|-------|-----------------------------------------------------|
| date  | Vital function -> Body weight: Date of measurement  |
| value | Vital function -> Body weight: Value of measurement |
| unit  | Vital function -> Body weight: Unit of measurement  |

#### 0+ blood transfusions

| Field   | Origin                     |
|---------|----------------------------|
| date    | Blood transfusion: Date    |
| product | Blood transfusion: Product |

#### 0+ medications

| Field                            | Example Value                                                                | Origin                                   |
|----------------------------------|------------------------------------------------------------------------------|------------------------------------------|
| name                             | Ibuprofen                                                                    | Medication: name                         | 
| administrationRoute              | Oral                                                                         | Medication: Administration route         |
| status                           | ON_HOLD                                                                      | Added in curation                        |
| dosageMin                        | 750                                                                          | Medication: min dosage                   |
| dosageMax                        | 1000                                                                         | Medication: max dosage                   |
| dosageUnit                       | mg                                                                           | Medication: Dosage unit                  |
| frequency                        | 1                                                                            | Medication: Dosage frequency             |
| frequencyUnit                    | day                                                                          | Medication: Dosage frequency unit        |
| periodBetweenValue               | Months                                                                       | Medication: Period between dosages value |
| periodBetweenUnit                | 2                                                                            | Medication: PPeriod between dosages unit |
| ifNeeded                         | 0                                                                            | Medication: If needed                    |
| startDate                        | 2021-07-01                                                                   | Medication: Start date                   |
| stopDate                         | 2021-10-01                                                                   | Medication: Stop date                    |
| drug.name                        | "BEVACIZUMAB"                                                                | Added in curation                        |
| drug.category                    | "TARGETED THERAPY"                                                           | Added in curation                        |
| drug.drugTypes                   | "VEGF_ANTIBODY"                                                              | Added in curation                        |
| cypInteractions.type             | 'Inducer', 'Inhibitor' or 'Substrate'                                        | Added in curation                        |
| cypInteractions.strength         | 'Strong', 'Moderate', 'Weak', 'Sensitive', 'Moderate sensitive' or 'Unknown' | Added in curation                        |
| cypInteractions.name             | 2C8                                                                          | Added in curation                        |
| transporterInteractions.type     | 'Inducer', 'Inhibitor' or 'Substrate'                                        | Added in curation                        |
| transporterInteractions.strength | 'Strong', 'Moderate', 'Weak', 'Sensitive', 'Moderate sensitive' or 'Unknown' | Added in curation                        |
| transporterInteractions.name     | BCRP                                                                         | Added in curation                        |
| atc.anatomicalMainGroup.code     | N                                                                            | Optional/added in curation               |
| atc.anatomicalMainGroup.name     | NERVOUS SYSTEM                                                               | Optional/added in curation               |
| atc.therapeuticSubGroup.code     | N02                                                                          | Optional/added in curation               |
| atc.therapeuticSubGroup.name     | ANALGESICS                                                                   | Optional/added in curation               |
| atc.pharmacologicalSubGroup.code | N02A                                                                         | Optional/added in curation               |
| atc.pharmacologicalSubGroup.name | OPIOIDS                                                                      | Optional/added in curation               |
| atc.chemicalSubGroup.code        | N02AJ                                                                        | Optional/added in curation               |
| atc.chemicalSubGroup.name        | Opioids in combination with non-opioid analgesics                            | Optional/added in curation               |
| atc.chemicalSubstance.code       | N02AJ08                                                                      | Optional/added in curation               |
| atc.chemicalSubstance.name       | codeine and ibuprofen 	                                                      | Optional/added in curation               |
| qtProlongatingRisk               | NONE 	                                                                       | Added in curation                        |
