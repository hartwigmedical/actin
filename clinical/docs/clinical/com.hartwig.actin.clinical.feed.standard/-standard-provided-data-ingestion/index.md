---
title: StandardProvidedDataIngestion
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[StandardProvidedDataIngestion](index.html)



# StandardProvidedDataIngestion



[JVM]\
class [StandardProvidedDataIngestion](index.html)(directory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), medicationExtractor: [ProvidedMedicationExtractor](../-provided-medication-extractor/index.html), surgeryExtractor: [ProvidedSurgeryExtractor](../-provided-surgery-extractor/index.html), intolerancesExtractor: [ProvidedIntolerancesExtractor](../-provided-intolerances-extractor/index.html), vitalFunctionsExtractor: [ProvidedVitalFunctionsExtractor](../-provided-vital-functions-extractor/index.html), bloodTransfusionExtractor: [ProvidedBloodTransfusionExtractor](../-provided-blood-transfusion-extractor/index.html), labValuesExtractor: [ProvidedLabValuesExtractor](../-provided-lab-values-extractor/index.html), toxicityExtractor: [ProvidedToxicityExtractor](../-provided-toxicity-extractor/index.html), complicationExtractor: [ProvidedComplicationExtractor](../-provided-complication-extractor/index.html), priorOtherConditionsExtractor: [ProvidedPriorOtherConditionsExtractor](../-provided-prior-other-conditions-extractor/index.html), treatmentHistoryExtractor: [ProvidedTreatmentHistoryExtractor](../-provided-treatment-history-extractor/index.html), clinicalStatusExtractor: [ProvidedClinicalStatusExtractor](../-provided-clinical-status-extractor/index.html), tumorDetailsExtractor: [HospitalProvidedTumorDetailsExtractor](../-hospital-provided-tumor-details-extractor/index.html), secondPrimaryExtractor: [ProvidedPriorPrimariesExtractor](../-provided-prior-primaries-extractor/index.html), patientDetailsExtractor: [ProvidedPatientDetailsExtractor](../-provided-patient-details-extractor/index.html), bodyWeightExtractor: [ProvidedBodyWeightExtractor](../-provided-body-weight-extractor/index.html), bodyHeightExtractor: [ProvidedBodyHeightExtractor](../-provided-body-height-extractor/index.html), molecularTestExtractor: [ProvidedMolecularTestExtractor](../-provided-molecular-test-extractor/index.html), dataQualityMask: [DataQualityMask](../-data-quality-mask/index.html))



## Constructors


| | |
|---|---|
| [StandardProvidedDataIngestion](-standard-provided-data-ingestion.html) | [JVM]<br>constructor(directory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), medicationExtractor: [ProvidedMedicationExtractor](../-provided-medication-extractor/index.html), surgeryExtractor: [ProvidedSurgeryExtractor](../-provided-surgery-extractor/index.html), intolerancesExtractor: [ProvidedIntolerancesExtractor](../-provided-intolerances-extractor/index.html), vitalFunctionsExtractor: [ProvidedVitalFunctionsExtractor](../-provided-vital-functions-extractor/index.html), bloodTransfusionExtractor: [ProvidedBloodTransfusionExtractor](../-provided-blood-transfusion-extractor/index.html), labValuesExtractor: [ProvidedLabValuesExtractor](../-provided-lab-values-extractor/index.html), toxicityExtractor: [ProvidedToxicityExtractor](../-provided-toxicity-extractor/index.html), complicationExtractor: [ProvidedComplicationExtractor](../-provided-complication-extractor/index.html), priorOtherConditionsExtractor: [ProvidedPriorOtherConditionsExtractor](../-provided-prior-other-conditions-extractor/index.html), treatmentHistoryExtractor: [ProvidedTreatmentHistoryExtractor](../-provided-treatment-history-extractor/index.html), clinicalStatusExtractor: [ProvidedClinicalStatusExtractor](../-provided-clinical-status-extractor/index.html), tumorDetailsExtractor: [HospitalProvidedTumorDetailsExtractor](../-hospital-provided-tumor-details-extractor/index.html), secondPrimaryExtractor: [ProvidedPriorPrimariesExtractor](../-provided-prior-primaries-extractor/index.html), patientDetailsExtractor: [ProvidedPatientDetailsExtractor](../-provided-patient-details-extractor/index.html), bodyWeightExtractor: [ProvidedBodyWeightExtractor](../-provided-body-weight-extractor/index.html), bodyHeightExtractor: [ProvidedBodyHeightExtractor](../-provided-body-height-extractor/index.html), molecularTestExtractor: [ProvidedMolecularTestExtractor](../-provided-molecular-test-extractor/index.html), dataQualityMask: [DataQualityMask](../-data-quality-mask/index.html)) |


## Types


| Name | Summary |
|---|---|
| [Companion](-companion/index.html) | [JVM]<br>object [Companion](-companion/index.html) |


## Functions


| Name | Summary |
|---|---|
| [ingest](ingest.html) | [JVM]<br>open fun [ingest](ingest.html)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;&lt;Error class: unknown class&gt;, &lt;Error class: unknown class&gt;&gt;&gt; |

