---
title: ProvidedPatientRecord
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[ProvidedPatientRecord](index.html)



# ProvidedPatientRecord



[JVM]\
data class [ProvidedPatientRecord](index.html)(val allergies: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrAllergy](../-ehr-allergy/index.html)&gt; = emptyList(), val bloodTransfusions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedBloodTransfusion](../-provided-blood-transfusion/index.html)&gt; = emptyList(), val complications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedComplication](../-provided-complication/index.html)&gt; = emptyList(), val labValues: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedLabValue](../-provided-lab-value/index.html)&gt; = emptyList(), val medications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMedication](../-provided-medication/index.html)&gt;? = emptyList(), val molecularTestHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMolecularTest](../-provided-molecular-test/index.html)&gt; = emptyList(), val patientDetails: [ProvidedPatientDetail](../-provided-patient-detail/index.html), val priorOtherConditions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedPriorOtherCondition](../-provided-prior-other-condition/index.html)&gt; = emptyList(), val surgeries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedSurgery](../-provided-surgery/index.html)&gt; = emptyList(), val toxicities: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedToxicity](../-provided-toxicity/index.html)&gt; = emptyList(), val treatmentHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedTreatmentHistory](../-provided-treatment-history/index.html)&gt; = emptyList(), val tumorDetails: [ProvidedTumorDetail](../-provided-tumor-detail/index.html), val priorPrimaries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedPriorPrimary](../-provided-prior-primary/index.html)&gt; = emptyList(), val measurements: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMeasurement](../-provided-measurement/index.html)&gt; = emptyList(), val whoEvaluations: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedWhoEvaluation](../-provided-who-evaluation/index.html)&gt; = emptyList())

Data class representing a patient record in the EHR



## Constructors


| | |
|---|---|
| [ProvidedPatientRecord](-provided-patient-record.html) | [JVM]<br>constructor(allergies: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrAllergy](../-ehr-allergy/index.html)&gt; = emptyList(), bloodTransfusions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedBloodTransfusion](../-provided-blood-transfusion/index.html)&gt; = emptyList(), complications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedComplication](../-provided-complication/index.html)&gt; = emptyList(), labValues: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedLabValue](../-provided-lab-value/index.html)&gt; = emptyList(), medications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMedication](../-provided-medication/index.html)&gt;? = emptyList(), molecularTestHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMolecularTest](../-provided-molecular-test/index.html)&gt; = emptyList(), patientDetails: [ProvidedPatientDetail](../-provided-patient-detail/index.html), priorOtherConditions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedPriorOtherCondition](../-provided-prior-other-condition/index.html)&gt; = emptyList(), surgeries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedSurgery](../-provided-surgery/index.html)&gt; = emptyList(), toxicities: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedToxicity](../-provided-toxicity/index.html)&gt; = emptyList(), treatmentHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedTreatmentHistory](../-provided-treatment-history/index.html)&gt; = emptyList(), tumorDetails: [ProvidedTumorDetail](../-provided-tumor-detail/index.html), priorPrimaries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedPriorPrimary](../-provided-prior-primary/index.html)&gt; = emptyList(), measurements: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMeasurement](../-provided-measurement/index.html)&gt; = emptyList(), whoEvaluations: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedWhoEvaluation](../-provided-who-evaluation/index.html)&gt; = emptyList()) |


## Properties


| Name | Summary |
|---|---|
| [allergies](allergies.html) | [JVM]<br>val [allergies](allergies.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrAllergy](../-ehr-allergy/index.html)&gt; |
| [bloodTransfusions](blood-transfusions.html) | [JVM]<br>val [bloodTransfusions](blood-transfusions.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedBloodTransfusion](../-provided-blood-transfusion/index.html)&gt; |
| [complications](complications.html) | [JVM]<br>val [complications](complications.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedComplication](../-provided-complication/index.html)&gt; |
| [labValues](lab-values.html) | [JVM]<br>val [labValues](lab-values.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedLabValue](../-provided-lab-value/index.html)&gt; |
| [measurements](measurements.html) | [JVM]<br>val [measurements](measurements.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMeasurement](../-provided-measurement/index.html)&gt; |
| [medications](medications.html) | [JVM]<br>val [medications](medications.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMedication](../-provided-medication/index.html)&gt;? |
| [molecularTestHistory](molecular-test-history.html) | [JVM]<br>val [molecularTestHistory](molecular-test-history.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedMolecularTest](../-provided-molecular-test/index.html)&gt; |
| [patientDetails](patient-details.html) | [JVM]<br>val [patientDetails](patient-details.html): [ProvidedPatientDetail](../-provided-patient-detail/index.html) |
| [priorOtherConditions](prior-other-conditions.html) | [JVM]<br>val [priorOtherConditions](prior-other-conditions.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedPriorOtherCondition](../-provided-prior-other-condition/index.html)&gt; |
| [priorPrimaries](prior-primaries.html) | [JVM]<br>val [priorPrimaries](prior-primaries.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedPriorPrimary](../-provided-prior-primary/index.html)&gt; |
| [surgeries](surgeries.html) | [JVM]<br>val [surgeries](surgeries.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedSurgery](../-provided-surgery/index.html)&gt; |
| [toxicities](toxicities.html) | [JVM]<br>val [toxicities](toxicities.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedToxicity](../-provided-toxicity/index.html)&gt; |
| [treatmentHistory](treatment-history.html) | [JVM]<br>val [treatmentHistory](treatment-history.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedTreatmentHistory](../-provided-treatment-history/index.html)&gt; |
| [tumorDetails](tumor-details.html) | [JVM]<br>val [tumorDetails](tumor-details.html): [ProvidedTumorDetail](../-provided-tumor-detail/index.html) |
| [whoEvaluations](who-evaluations.html) | [JVM]<br>val [whoEvaluations](who-evaluations.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedWhoEvaluation](../-provided-who-evaluation/index.html)&gt; |

