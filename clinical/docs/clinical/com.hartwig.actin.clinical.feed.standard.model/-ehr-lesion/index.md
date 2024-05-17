//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard.model](../index.md)/[EhrLesion](index.md)

# EhrLesion

[JVM]\
data class [EhrLesion](index.md)(val location: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val subLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html))

Data class representing a lesion in the EHR

## Constructors

| | |
|---|---|
| [EhrLesion](-ehr-lesion.md) | [JVM]<br>constructor(location: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), subLocation: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, diagnosisDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)) |

## Properties

| Name | Summary |
|---|---|
| [diagnosisDate](diagnosis-date.md) | [JVM]<br>val [diagnosisDate](diagnosis-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Diagnosis date of the lesion |
| [location](location.md) | [JVM]<br>val [location](location.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Location of the lesion |
| [subLocation](sub-location.md) | [JVM]<br>val [subLocation](sub-location.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Sub location of the lesion |
