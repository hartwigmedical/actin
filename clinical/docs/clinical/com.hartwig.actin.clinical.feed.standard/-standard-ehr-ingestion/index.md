//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[StandardEhrIngestion](index.md)

# StandardEhrIngestion

[JVM]\
class [StandardEhrIngestion](index.md)(directory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), medicationExtractor: [EhrMedicationExtractor](../-ehr-medication-extractor/index.md), surgeryExtractor: [EhrSurgeryExtractor](../-ehr-surgery-extractor/index.md), intolerancesExtractor: [EhrIntolerancesExtractor](../-ehr-intolerances-extractor/index.md), vitalFunctionsExtractor: [EhrVitalFunctionsExtractor](../-ehr-vital-functions-extractor/index.md), bloodTransfusionExtractor: [EhrBloodTransfusionExtractor](../-ehr-blood-transfusion-extractor/index.md), labValuesExtractor: [EhrLabValuesExtractor](../-ehr-lab-values-extractor/index.md), toxicityExtractor: [EhrToxicityExtractor](../-ehr-toxicity-extractor/index.md), complicationExtractor: [EhrComplicationExtractor](../-ehr-complication-extractor/index.md), priorOtherConditionsExtractor: [EhrPriorOtherConditionsExtractor](../-ehr-prior-other-conditions-extractor/index.md), treatmentHistoryExtractor: [EhrTreatmentHistoryExtractor](../-ehr-treatment-history-extractor/index.md), clinicalStatusExtractor: [EhrClinicalStatusExtractor](../-ehr-clinical-status-extractor/index.md), tumorDetailsExtractor: [EhrTumorDetailsExtractor](../-ehr-tumor-details-extractor/index.md), secondPrimaryExtractor: [EhrPriorPrimariesExtractor](../-ehr-prior-primaries-extractor/index.md), patientDetailsExtractor: [EhrPatientDetailsExtractor](../-ehr-patient-details-extractor/index.md), bodyWeightExtractor: [EhrBodyWeightExtractor](../-ehr-body-weight-extractor/index.md), bodyHeightExtractor: [EhrBodyHeightExtractor](../-ehr-body-height-extractor/index.md), molecularTestExtractor: [EhrMolecularTestExtractor](../-ehr-molecular-test-extractor/index.md), dataQualityMask: [DataQualityMask](../-data-quality-mask/index.md))

## Constructors

| | |
|---|---|
| [StandardEhrIngestion](-standard-ehr-ingestion.md) | [JVM]<br>constructor(directory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), medicationExtractor: [EhrMedicationExtractor](../-ehr-medication-extractor/index.md), surgeryExtractor: [EhrSurgeryExtractor](../-ehr-surgery-extractor/index.md), intolerancesExtractor: [EhrIntolerancesExtractor](../-ehr-intolerances-extractor/index.md), vitalFunctionsExtractor: [EhrVitalFunctionsExtractor](../-ehr-vital-functions-extractor/index.md), bloodTransfusionExtractor: [EhrBloodTransfusionExtractor](../-ehr-blood-transfusion-extractor/index.md), labValuesExtractor: [EhrLabValuesExtractor](../-ehr-lab-values-extractor/index.md), toxicityExtractor: [EhrToxicityExtractor](../-ehr-toxicity-extractor/index.md), complicationExtractor: [EhrComplicationExtractor](../-ehr-complication-extractor/index.md), priorOtherConditionsExtractor: [EhrPriorOtherConditionsExtractor](../-ehr-prior-other-conditions-extractor/index.md), treatmentHistoryExtractor: [EhrTreatmentHistoryExtractor](../-ehr-treatment-history-extractor/index.md), clinicalStatusExtractor: [EhrClinicalStatusExtractor](../-ehr-clinical-status-extractor/index.md), tumorDetailsExtractor: [EhrTumorDetailsExtractor](../-ehr-tumor-details-extractor/index.md), secondPrimaryExtractor: [EhrPriorPrimariesExtractor](../-ehr-prior-primaries-extractor/index.md), patientDetailsExtractor: [EhrPatientDetailsExtractor](../-ehr-patient-details-extractor/index.md), bodyWeightExtractor: [EhrBodyWeightExtractor](../-ehr-body-weight-extractor/index.md), bodyHeightExtractor: [EhrBodyHeightExtractor](../-ehr-body-height-extractor/index.md), molecularTestExtractor: [EhrMolecularTestExtractor](../-ehr-molecular-test-extractor/index.md), dataQualityMask: [DataQualityMask](../-data-quality-mask/index.md)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [JVM]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [ingest](ingest.md) | [JVM]<br>open fun [ingest](ingest.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;&lt;Error class: unknown class&gt;, &lt;Error class: unknown class&gt;&gt;&gt; |
