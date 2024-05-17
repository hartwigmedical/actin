---
title: ProvidedToxicity
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[ProvidedToxicity](index.html)



# ProvidedToxicity



[JVM]\
data class [ProvidedToxicity](index.html)(val name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val categories: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, val evaluatedDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val grade: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

Data class representing a toxicity in the EHR



## Constructors


| | |
|---|---|
| [ProvidedToxicity](-provided-toxicity.html) | [JVM]<br>constructor(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), categories: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, evaluatedDate: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), grade: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [categories](categories.html) | [JVM]<br>val [categories](categories.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;<br>Categories of the toxicity |
| [evaluatedDate](evaluated-date.html) | [JVM]<br>val [evaluatedDate](evaluated-date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Evaluated date of the toxicity |
| [grade](grade.html) | [JVM]<br>val [grade](grade.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Grade of the toxicity |
| [name](name.html) | [JVM]<br>val [name](name.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Name of the toxicity |

