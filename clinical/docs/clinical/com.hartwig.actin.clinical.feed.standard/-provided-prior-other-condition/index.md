---
title: ProvidedPriorOtherCondition
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[ProvidedPriorOtherCondition](index.html)



# ProvidedPriorOtherCondition



[JVM]\
data class [ProvidedPriorOtherCondition](index.html)(val name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, val startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null)

Data class representing a prior other condition in the EHR



## Constructors


| | |
|---|---|
| [ProvidedPriorOtherCondition](-provided-prior-other-condition.html) | [JVM]<br>constructor(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null) |


## Properties


| Name | Summary |
|---|---|
| [category](category.html) | [JVM]<br>val [category](category.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null<br>Category of the prior other condition |
| [endDate](end-date.html) | [JVM]<br>val [endDate](end-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)? = null<br>End date of the prior other condition |
| [name](name.html) | [JVM]<br>val [name](name.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Name of the prior other condition |
| [startDate](start-date.html) | [JVM]<br>val [startDate](start-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Start date of the prior other condition |

