## ACTIN-Clinical

ACTIN-Clinical ingests (external) clinical feed and uses an internal curation database to create data in terms of the ACTIN datamodel.
Clinical data in the ACTIN datamodel is written to a per-patient json file. The clinical data can be loaded into a mysql database
via [ACTIN-Database](../database/README.md).

This application requires Java 11+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.clinical.ClinicalIngestionApplicationKt \
   -feed_directory /path/to/feed_file_dir \
   -curation_directory /path/to/curation_file_dir \
   -treatment_directory /path/to/treatment_db_dir \
   -doid_json /path/to/full_doid_tree_json_file \
   -atc_tsv /path/to/atc_tsv \
   -atc_overrides_tsv /path/to/atc_overrides_tsv \
   -output_directory /path/to/where/clinical_json_files/are/written
```

## ACTIN clinical model for providers

Data providers can provide clinical data in JSON adhering to the [following format]()


## ACTIN clinical datamodel used internally 

In ACTIN, the clinical feed as described above, is mapped onto the ACTIN clinical data model.

### Disease Ontology ID

For mapping of primary tumor location and type, second primaries and other conditions in the ACTIN clinical datamodel,
one or more Disease Ontology IDs (DOIDs) are assigned. For more information, see https://disease-ontology.org/.

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

#### 1 clinical status

| Field                      | Origin                         |
|----------------------------|--------------------------------|
| who                        | Patient: WHO                   |
| hasActiveInfection         | Other relevant patient history |
| activeInfectionDescription | Other relevant patient history |
| hasToxicitiesGrade2        | Toxicity details               |
| hasSigAberrationLatestECG  | ECG details                    |
| ecgAberrationDescription   | ECG details                    |
| qtcfValue                  | ECG details                    |
| qtcfUnit                   | ECG details                    |
| lvef                       | ECG details                    |
| hasComplications           | Complication details           |

#### N treatment history entries in oncological history

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

#### N prior second primaries

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

#### N prior other conditions

| Field                        | Origin                      |
|------------------------------|-----------------------------|
| name                         | Other condition: name       |
| year                         | Other condition: start date |
| month                        | Other condition: start date |
| doids                        | Added in curation           |
| category                     | Added in curation           |
| isContraindicationForTherapy | Added in curation           |

#### N prior (non-WGS) molecular tests

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

#### N cancer related complications

| Field      | Origin                   |
|------------|--------------------------|
| name       | Complication: name       |
| categories | Added in curation        |
| year       | Complication: start date |
| month      | Complication: start date |

#### N lab values

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

#### N toxicities

| Field         | Origin            |
|---------------|-------------------|
| name          | Toxicities: Name  |
| evaluatedDate | Toxicities: Date  |
| grade         | Toxicities: Grade |
| source        | If applicable     |

#### N intolerances

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

#### N surgeries

| Field   | Origin        |
|---------|---------------|
| endDate | If applicable |
| status  | If applicable |

#### N vital function measurements

| Field       | Origin                |
|-------------|-----------------------|
| date        | Vital function: Date  |
| category    | Vital function: Name  |
| subcategory | Vital function: Name  |
| value       | Vital function: Value |
| unit        | Vital function: Unit  |

#### N body weight measurements

| Field | Origin                                              |
|-------|-----------------------------------------------------|
| date  | Vital function -> Body weight: Date of measurement  |
| value | Vital function -> Body weight: Value of measurement |
| unit  | Vital function -> Body weight: Unit of measurement  |

#### N blood transfusions

| Field   | Origin                     |
|---------|----------------------------|
| date    | Blood transfusion: Date    |
| product | Blood transfusion: Product |

#### N medications

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

### Filtering of Clinical Data

When the clinical data is mapped onto the ACTIN clinical data model, as described above, filtering is applied to exclude non-valid
measurements from evaluation.
Filtering is applied for the following variables (excluded values in parentheses):

- Body weight (<20 or >300 kg)
- Vital functions: heart rate (<10, >300), blood pressure (<10, >300) , pulse oximetry (<10, >100).

To prevent loss of data, excluded values are mapped like all other values. However, the valid property (Boolean) is set to false, excluding
them from
further evaluation.
Values with the wrong unit are not flagged as invalid, but handled separately inside each separate evaluating (vital function or body
weight) class. If only values with the wrong unit are present among recent measurements, a specific warning is shown.

=======
## Treatment Database

The treatment database is loaded from 2 JSON files that define its contents.

### treatment.json

treatment.json consists of an array of treatment entries with some common fields:

- name: The unique identifier for the treatment.
- synonyms: A list of synonyms for the treatment used for matching.
- isSystemic: Boolean indicating whether the treatment is systemic or not.
- treatmentClass: The class of treatment described by this entry - OtherTreatment, DrugTreatment, or Radiotherapy.

Each treatment can be described by a set of categories (defined in TreatmentCategory) and a set of types, each of which belongs to a
category.
OtherTreatment entries define these directly in these fields:

- categories: The list of categories that apply to the treatment, which can be inferred by the types when they're provided.
- types: The list of types that describe the treatment.

DrugTreatment entries omit the "categories" and "types" fields because these attributes are inherited from the drugs included in the "drugs"
field.
Each entry in "drugs" corresponds to the name of a drug defined in drug.json. A DrugTreatment may also include a "maxCycles" integer field.

Radiotherapy entries also omit the "categories" and "types" fields. A Radiotherapy's category is always RADIOTHERAPY, and it may optionally
have a RadiotherapyType provided in "radioType".

### drug.json

drug.json consists of an array of drug entries with the following fields:

- name: The unique name of this drug used to reference it elsewhere, such as in treatment.json.
- drugTypes: The list of entries from DrugType that describe this drug.
- category: The TreatmentCategory of this drug.

### Filtering of Clinical Data

When the clinical data is mapped onto the ACTIN clinical data model, as described above, filtering is applied to exclude non-valid
measurements.
Filtering is applied for the following variables (excluded values in parentheses):

- Body weight (<20 or >300 kg)
- Vital functions: heart rate (<10, >300), blood pressure (<10, >300) , pulse oximetry (<10, >100).

To prevent loss of data, excluded values are mapped like all other values. However, the unit is changed to \<ignore>, excluding them from
further evaluation.

### Version History and Download Links

- Upcoming (first release) 
