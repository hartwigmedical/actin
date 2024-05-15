//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[EhrMedication](index.md)

# EhrMedication

[JVM]\
data class [EhrMedication](index.md)(val name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val atcCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?, val endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?, val administrationRoute: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val dosage: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?, val dosageUnit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val frequency: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?, val frequencyUnit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val periodBetweenDosagesValue: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?, val periodBetweenDosagesUnit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val administrationOnlyIfNeeded: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)?, val isTrial: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), val isSelfCare: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))

Data class representing a medication in the EHR

## Constructors

| | |
|---|---|
| [EhrMedication](-ehr-medication.md) | [JVM]<br>constructor(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), atcCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?, endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?, administrationRoute: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, dosage: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?, dosageUnit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, frequency: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?, frequencyUnit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, periodBetweenDosagesValue: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?, periodBetweenDosagesUnit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, administrationOnlyIfNeeded: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)?, isTrial: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), isSelfCare: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [administrationOnlyIfNeeded](administration-only-if-needed.md) | [JVM]<br>val [administrationOnlyIfNeeded](administration-only-if-needed.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)?<br>Administration only if needed of the medication |
| [administrationRoute](administration-route.md) | [JVM]<br>val [administrationRoute](administration-route.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Administration route of the medication |
| [atcCode](atc-code.md) | [JVM]<br>val [atcCode](atc-code.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>ATC code of the medication |
| [dosage](dosage.md) | [JVM]<br>val [dosage](dosage.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?<br>Dosage of the medication |
| [dosageUnit](dosage-unit.md) | [JVM]<br>val [dosageUnit](dosage-unit.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Dosage unit of the medication |
| [endDate](end-date.md) | [JVM]<br>val [endDate](end-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?<br>End date of the medication |
| [frequency](frequency.md) | [JVM]<br>val [frequency](frequency.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?<br>Frequency of the medication |
| [frequencyUnit](frequency-unit.md) | [JVM]<br>val [frequencyUnit](frequency-unit.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Frequency unit of the medication |
| [isSelfCare](is-self-care.md) | [JVM]<br>val [isSelfCare](is-self-care.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Is self care of the medication |
| [isTrial](is-trial.md) | [JVM]<br>val [isTrial](is-trial.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Is trial of the medication |
| [name](name.md) | [JVM]<br>val [name](name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Name of the medication |
| [periodBetweenDosagesUnit](period-between-dosages-unit.md) | [JVM]<br>val [periodBetweenDosagesUnit](period-between-dosages-unit.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Period between dosages unit of the medication |
| [periodBetweenDosagesValue](period-between-dosages-value.md) | [JVM]<br>val [periodBetweenDosagesValue](period-between-dosages-value.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?<br>Period between dosages value of the medication |
| [startDate](start-date.md) | [JVM]<br>val [startDate](start-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?<br>Start date of the medication |
