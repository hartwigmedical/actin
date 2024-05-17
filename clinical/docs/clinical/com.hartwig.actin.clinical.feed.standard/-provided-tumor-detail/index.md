---
title: ProvidedTumorDetail
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[ProvidedTumorDetail](index.html)



# ProvidedTumorDetail



[JVM]\
data class [ProvidedTumorDetail](index.html)(val diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val tumorLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val tumorType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val tumorGradeDifferentiation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val tumorStage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val tumorStageDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, val measurableDisease: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = null, val measurableDiseaseDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, val lesions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedLesion](../-provided-lesion/index.html)&gt;? = null, val lesionSite: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null)

Data class representing a tumor detail in the EHR



## Constructors


| | |
|---|---|
| [ProvidedTumorDetail](-provided-tumor-detail.html) | [JVM]<br>constructor(diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), tumorLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), tumorType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), tumorGradeDifferentiation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, tumorStage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, tumorStageDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, measurableDisease: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = null, measurableDiseaseDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, lesions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedLesion](../-provided-lesion/index.html)&gt;? = null, lesionSite: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null) |


## Properties


| Name | Summary |
|---|---|
| [diagnosisDate](diagnosis-date.html) | [JVM]<br>val [diagnosisDate](diagnosis-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Diagnosis date of the tumor |
| [lesions](lesions.html) | [JVM]<br>val [lesions](lesions.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[ProvidedLesion](../-provided-lesion/index.html)&gt;? = null<br>Lesions of the tumor |
| [lesionSite](lesion-site.html) | [JVM]<br>val [lesionSite](lesion-site.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Lesion site of the tumor |
| [measurableDisease](measurable-disease.html) | [JVM]<br>val [measurableDisease](measurable-disease.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)? = null<br>Measurable disease of the tumor |
| [measurableDiseaseDate](measurable-disease-date.html) | [JVM]<br>val [measurableDiseaseDate](measurable-disease-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>Measurable disease date of the tumor |
| [tumorGradeDifferentiation](tumor-grade-differentiation.html) | [JVM]<br>val [tumorGradeDifferentiation](tumor-grade-differentiation.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Grade differentiation of the tumor |
| [tumorLocation](tumor-location.html) | [JVM]<br>val [tumorLocation](tumor-location.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Location of the tumor |
| [tumorStage](tumor-stage.html) | [JVM]<br>val [tumorStage](tumor-stage.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Stage of the tumor |
| [tumorStageDate](tumor-stage-date.html) | [JVM]<br>val [tumorStageDate](tumor-stage-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>Stage date of the tumor |
| [tumorType](tumor-type.html) | [JVM]<br>val [tumorType](tumor-type.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Type of the tumor |

