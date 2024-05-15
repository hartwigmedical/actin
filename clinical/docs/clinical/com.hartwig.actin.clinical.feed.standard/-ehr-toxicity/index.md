//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[EhrToxicity](index.md)

# EhrToxicity

[JVM]\
data class [EhrToxicity](index.md)(val name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val categories: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, val evaluatedDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val grade: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

Data class representing a toxicity in the EHR

## Constructors

| | |
|---|---|
| [EhrToxicity](-ehr-toxicity.md) | [JVM]<br>constructor(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), categories: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, evaluatedDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), grade: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [categories](categories.md) | [JVM]<br>val [categories](categories.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;<br>Categories of the toxicity |
| [evaluatedDate](evaluated-date.md) | [JVM]<br>val [evaluatedDate](evaluated-date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Evaluated date of the toxicity |
| [grade](grade.md) | [JVM]<br>val [grade](grade.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Grade of the toxicity |
| [name](name.md) | [JVM]<br>val [name](name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Name of the toxicity |
