//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[EhrComplication](index.md)

# EhrComplication

[JVM]\
data class [EhrComplication](index.md)(val name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val categories: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt; = emptyList(), val startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?)

Data class representing a complication in the EHR

## Constructors

| | |
|---|---|
| [EhrComplication](-ehr-complication.md) | [JVM]<br>constructor(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), categories: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt; = emptyList(), startDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), endDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?) |

## Properties

| Name | Summary |
|---|---|
| [categories](categories.md) | [JVM]<br>val [categories](categories.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;<br>Categories of the complication |
| [endDate](end-date.md) | [JVM]<br>val [endDate](end-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)?<br>End date of the complication |
| [name](name.md) | [JVM]<br>val [name](name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Name of the complication |
| [startDate](start-date.md) | [JVM]<br>val [startDate](start-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Start date of the complication |
