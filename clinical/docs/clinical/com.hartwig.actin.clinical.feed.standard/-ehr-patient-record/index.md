---
title: EhrPatientRecord
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[EhrPatientRecord](index.html)



# EhrPatientRecord



[JVM]\
data class [EhrPatientRecord](index.html)(val allergies: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrAllergy](../-ehr-allergy/index.html)&gt; = emptyList(), val bloodTransfusions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrBloodTransfusion](../-ehr-blood-transfusion/index.html)&gt; = emptyList(), val complications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrComplication](../-ehr-complication/index.html)&gt; = emptyList(), val labValues: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrLabValue](../-ehr-lab-value/index.html)&gt; = emptyList(), val medications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMedication](../-ehr-medication/index.html)&gt;? = emptyList(), val molecularTestHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMolecularTest](../-ehr-molecular-test/index.html)&gt; = emptyList(), val patientDetails: [EhrPatientDetail](../-ehr-patient-detail/index.html), val priorOtherConditions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrPriorOtherCondition](../-ehr-prior-other-condition/index.html)&gt; = emptyList(), val surgeries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrSurgery](../-ehr-surgery/index.html)&gt; = emptyList(), val toxicities: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrToxicity](../-ehr-toxicity/index.html)&gt; = emptyList(), val treatmentHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrTreatmentHistory](../-ehr-treatment-history/index.html)&gt; = emptyList(), val tumorDetails: [EhrTumorDetail](../-ehr-tumor-detail/index.html), val priorPrimaries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrPriorPrimary](../-ehr-prior-primary/index.html)&gt; = emptyList(), val measurements: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMeasurement](../-ehr-measurement/index.html)&gt; = emptyList(), val whoEvaluations: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrWhoEvaluation](../-ehr-who-evaluation/index.html)&gt; = emptyList())

Data class representing a patient record in the EHR



## Constructors


| | |
|---|---|
| [EhrPatientRecord](-ehr-patient-record.html) | [JVM]<br>constructor(allergies: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrAllergy](../-ehr-allergy/index.html)&gt; = emptyList(), bloodTransfusions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrBloodTransfusion](../-ehr-blood-transfusion/index.html)&gt; = emptyList(), complications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrComplication](../-ehr-complication/index.html)&gt; = emptyList(), labValues: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrLabValue](../-ehr-lab-value/index.html)&gt; = emptyList(), medications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMedication](../-ehr-medication/index.html)&gt;? = emptyList(), molecularTestHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMolecularTest](../-ehr-molecular-test/index.html)&gt; = emptyList(), patientDetails: [EhrPatientDetail](../-ehr-patient-detail/index.html), priorOtherConditions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrPriorOtherCondition](../-ehr-prior-other-condition/index.html)&gt; = emptyList(), surgeries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrSurgery](../-ehr-surgery/index.html)&gt; = emptyList(), toxicities: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrToxicity](../-ehr-toxicity/index.html)&gt; = emptyList(), treatmentHistory: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrTreatmentHistory](../-ehr-treatment-history/index.html)&gt; = emptyList(), tumorDetails: [EhrTumorDetail](../-ehr-tumor-detail/index.html), priorPrimaries: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrPriorPrimary](../-ehr-prior-primary/index.html)&gt; = emptyList(), measurements: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMeasurement](../-ehr-measurement/index.html)&gt; = emptyList(), whoEvaluations: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrWhoEvaluation](../-ehr-who-evaluation/index.html)&gt; = emptyList()) |


## Properties


| Name | Summary |
|---|---|
| [allergies](allergies.html) | [JVM]<br>val [allergies](allergies.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrAllergy](../-ehr-allergy/index.html)&gt; |
| [bloodTransfusions](blood-transfusions.html) | [JVM]<br>val [bloodTransfusions](blood-transfusions.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrBloodTransfusion](../-ehr-blood-transfusion/index.html)&gt; |
| [complications](complications.html) | [JVM]<br>val [complications](complications.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrComplication](../-ehr-complication/index.html)&gt; |
| [labValues](lab-values.html) | [JVM]<br>val [labValues](lab-values.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrLabValue](../-ehr-lab-value/index.html)&gt; |
| [measurements](measurements.html) | [JVM]<br>val [measurements](measurements.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMeasurement](../-ehr-measurement/index.html)&gt; |
| [medications](medications.html) | [JVM]<br>val [medications](medications.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMedication](../-ehr-medication/index.html)&gt;? |
| [molecularTestHistory](molecular-test-history.html) | [JVM]<br>val [molecularTestHistory](molecular-test-history.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrMolecularTest](../-ehr-molecular-test/index.html)&gt; |
| [patientDetails](patient-details.html) | [JVM]<br>val [patientDetails](patient-details.html): [EhrPatientDetail](../-ehr-patient-detail/index.html) |
| [priorOtherConditions](prior-other-conditions.html) | [JVM]<br>val [priorOtherConditions](prior-other-conditions.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrPriorOtherCondition](../-ehr-prior-other-condition/index.html)&gt; |
| [priorPrimaries](prior-primaries.html) | [JVM]<br>val [priorPrimaries](prior-primaries.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrPriorPrimary](../-ehr-prior-primary/index.html)&gt; |
| [surgeries](surgeries.html) | [JVM]<br>val [surgeries](surgeries.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrSurgery](../-ehr-surgery/index.html)&gt; |
| [toxicities](toxicities.html) | [JVM]<br>val [toxicities](toxicities.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrToxicity](../-ehr-toxicity/index.html)&gt; |
| [treatmentHistory](treatment-history.html) | [JVM]<br>val [treatmentHistory](treatment-history.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrTreatmentHistory](../-ehr-treatment-history/index.html)&gt; |
| [tumorDetails](tumor-details.html) | [JVM]<br>val [tumorDetails](tumor-details.html): [EhrTumorDetail](../-ehr-tumor-detail/index.html) |
| [whoEvaluations](who-evaluations.html) | [JVM]<br>val [whoEvaluations](who-evaluations.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrWhoEvaluation](../-ehr-who-evaluation/index.html)&gt; |

