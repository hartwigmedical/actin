//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard.model](../index.md)/[EhrPriorPrimary](index.md)

# EhrPriorPrimary

[JVM]\
data class [EhrPriorPrimary](index.md)(val diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?, val tumorLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val tumorType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val status: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val statusDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null)

Data class representing a prior primary in the EHR

## Constructors

| | |
|---|---|
| [EhrPriorPrimary](-ehr-prior-primary.md) | [JVM]<br>constructor(diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?, tumorLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), tumorType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), status: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, statusDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [diagnosisDate](diagnosis-date.md) | [JVM]<br>val [diagnosisDate](diagnosis-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?<br>Diagnosis date of the prior primary |
| [status](status.md) | [JVM]<br>val [status](status.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Status of the prior primary |
| [statusDate](status-date.md) | [JVM]<br>val [statusDate](status-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>Status date of the prior primary |
| [tumorLocation](tumor-location.md) | [JVM]<br>val [tumorLocation](tumor-location.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Location of the prior primary |
| [tumorType](tumor-type.md) | [JVM]<br>val [tumorType](tumor-type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Type of the prior primary |
