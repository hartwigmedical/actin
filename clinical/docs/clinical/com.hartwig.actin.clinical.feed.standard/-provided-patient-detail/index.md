---
title: ProvidedPatientDetail
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[ProvidedPatientDetail](index.html)



# ProvidedPatientDetail



[JVM]\
data class [ProvidedPatientDetail](index.html)(val birthYear: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val gender: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val registrationDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val hashedId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

General details of a patient



## Constructors


| | |
|---|---|
| [ProvidedPatientDetail](-provided-patient-detail.html) | [JVM]<br>constructor(birthYear: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), gender: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), registrationDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), hashedId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [birthYear](birth-year.html) | [JVM]<br>val [birthYear](birth-year.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Birth year of the patient eg. 1940) |
| [gender](gender.html) | [JVM]<br>val [gender](gender.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>gender of the patient eg. MALE, FEMALE, OTHER |
| [hashedId](hashed-id.html) | [JVM]<br>val [hashedId](hashed-id.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Base64 encoded SHA-256 hash of source hospital's identifier, |
| [registrationDate](registration-date.html) | [JVM]<br>val [registrationDate](registration-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>ACTIN registration date of the patient |

