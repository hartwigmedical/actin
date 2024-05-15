---
title: EhrTreatmentHistory
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[EhrTreatmentHistory](index.html)



# EhrTreatmentHistory



[JVM]\
data class [EhrTreatmentHistory](index.html)(val treatmentName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val intention: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, val stopReason: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val stopReasonDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, val response: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val responseDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, val intendedCycles: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val administeredCycles: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val modifications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrTreatmentModification](../-ehr-treatment-modification/index.html)&gt;? = null, val administeredInStudy: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))

Data class representing a treatment history in the EHR



## Constructors


| | |
|---|---|
| [EhrTreatmentHistory](-ehr-treatment-history.html) | [JVM]<br>constructor(treatmentName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), intention: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, stopReason: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, stopReasonDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, response: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, responseDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null, intendedCycles: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), administeredCycles: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), modifications: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrTreatmentModification](../-ehr-treatment-modification/index.html)&gt;? = null, administeredInStudy: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [administeredCycles](administered-cycles.html) | [JVM]<br>val [administeredCycles](administered-cycles.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Administered cycles of the treatment |
| [administeredInStudy](administered-in-study.html) | [JVM]<br>val [administeredInStudy](administered-in-study.html): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Administered in study of the treatment |
| [endDate](end-date.html) | [JVM]<br>val [endDate](end-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>End date of the treatment |
| [intendedCycles](intended-cycles.html) | [JVM]<br>val [intendedCycles](intended-cycles.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Intended cycles of the treatment |
| [intention](intention.html) | [JVM]<br>val [intention](intention.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Intention of the treatment |
| [modifications](modifications.html) | [JVM]<br>val [modifications](modifications.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EhrTreatmentModification](../-ehr-treatment-modification/index.html)&gt;? = null<br>Modifications of the treatment |
| [response](response.html) | [JVM]<br>val [response](response.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Response of the treatment |
| [responseDate](response-date.html) | [JVM]<br>val [responseDate](response-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>Response date of the treatment |
| [startDate](start-date.html) | [JVM]<br>val [startDate](start-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Start date of the treatment |
| [stopReason](stop-reason.html) | [JVM]<br>val [stopReason](stop-reason.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Stop reason of the treatment |
| [stopReasonDate](stop-reason-date.html) | [JVM]<br>val [stopReasonDate](stop-reason-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>Stop reason date of the treatment |
| [treatmentName](treatment-name.html) | [JVM]<br>val [treatmentName](treatment-name.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Name of the treatment |

