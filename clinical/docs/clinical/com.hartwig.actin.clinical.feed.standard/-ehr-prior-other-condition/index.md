//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[EhrPriorOtherCondition](index.md)

# EhrPriorOtherCondition

[JVM]\
data class [EhrPriorOtherCondition](index.md)(val name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null)

Data class representing a prior other condition in the EHR

## Constructors

| | |
|---|---|
| [EhrPriorOtherCondition](-ehr-prior-other-condition.md) | [JVM]<br>constructor(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [category](category.md) | [JVM]<br>val [category](category.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Category of the prior other condition |
| [endDate](end-date.md) | [JVM]<br>val [endDate](end-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>End date of the prior other condition |
| [name](name.md) | [JVM]<br>val [name](name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Name of the prior other condition |
| [startDate](start-date.md) | [JVM]<br>val [startDate](start-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Start date of the prior other condition |
