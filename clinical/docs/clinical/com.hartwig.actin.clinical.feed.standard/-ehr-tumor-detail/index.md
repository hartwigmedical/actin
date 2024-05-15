//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[EhrTumorDetail](index.md)

# EhrTumorDetail

[JVM]\
data class [EhrTumorDetail](index.md)(val diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val tumorLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val tumorType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val tumorGradeDifferentiation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val tumorStage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val tumorStageDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, val measurableDisease: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = null, val measurableDiseaseDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, val lesions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrLesion](../-ehr-lesion/index.md)&gt;? = null, val lesionSite: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null)

Data class representing a tumor detail in the EHR

## Constructors

| | |
|---|---|
| [EhrTumorDetail](-ehr-tumor-detail.md) | [JVM]<br>constructor(diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), tumorLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), tumorType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), tumorGradeDifferentiation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, tumorStage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, tumorStageDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, measurableDisease: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = null, measurableDiseaseDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, lesions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrLesion](../-ehr-lesion/index.md)&gt;? = null, lesionSite: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [diagnosisDate](diagnosis-date.md) | [JVM]<br>val [diagnosisDate](diagnosis-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Diagnosis date of the tumor |
| [lesions](lesions.md) | [JVM]<br>val [lesions](lesions.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrLesion](../-ehr-lesion/index.md)&gt;? = null<br>Lesions of the tumor |
| [lesionSite](lesion-site.md) | [JVM]<br>val [lesionSite](lesion-site.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Lesion site of the tumor |
| [measurableDisease](measurable-disease.md) | [JVM]<br>val [measurableDisease](measurable-disease.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = null<br>Measurable disease of the tumor |
| [measurableDiseaseDate](measurable-disease-date.md) | [JVM]<br>val [measurableDiseaseDate](measurable-disease-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>Measurable disease date of the tumor |
| [tumorGradeDifferentiation](tumor-grade-differentiation.md) | [JVM]<br>val [tumorGradeDifferentiation](tumor-grade-differentiation.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Grade differentiation of the tumor |
| [tumorLocation](tumor-location.md) | [JVM]<br>val [tumorLocation](tumor-location.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Location of the tumor |
| [tumorStage](tumor-stage.md) | [JVM]<br>val [tumorStage](tumor-stage.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Stage of the tumor |
| [tumorStageDate](tumor-stage-date.md) | [JVM]<br>val [tumorStageDate](tumor-stage-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>Stage date of the tumor |
| [tumorType](tumor-type.md) | [JVM]<br>val [tumorType](tumor-type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Type of the tumor |
