---
title: ProvidedLabValue
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[ProvidedLabValue](index.html)



# ProvidedLabValue



[JVM]\
data class [ProvidedLabValue](index.html)(val evaluationTime: [LocalDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html), val measure: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val measureCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val refUpperBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val refLowerBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val comparator: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val refFlag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

Data class representing a lab value in the EHR



## Constructors


| | |
|---|---|
| [ProvidedLabValue](-provided-lab-value.html) | [JVM]<br>constructor(evaluationTime: [LocalDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html), measure: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), measureCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, refUpperBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), refLowerBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), comparator: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, refFlag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [comparator](comparator.html) | [JVM]<br>val [comparator](comparator.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Comparator of the lab value |
| [evaluationTime](evaluation-time.html) | [JVM]<br>val [evaluationTime](evaluation-time.html): [LocalDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html)<br>Evaluation time of the lab value |
| [measure](measure.html) | [JVM]<br>val [measure](measure.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Measure of the lab value |
| [measureCode](measure-code.html) | [JVM]<br>val [measureCode](measure-code.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Measure code of the lab value |
| [refFlag](ref-flag.html) | [JVM]<br>val [refFlag](ref-flag.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Reference flag of the lab value |
| [refLowerBound](ref-lower-bound.html) | [JVM]<br>val [refLowerBound](ref-lower-bound.html): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Reference lower bound of the lab value |
| [refUpperBound](ref-upper-bound.html) | [JVM]<br>val [refUpperBound](ref-upper-bound.html): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Reference upper bound of the lab value |
| [unit](unit.html) | [JVM]<br>val [unit](unit.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Unit of the lab value |
| [value](value.html) | [JVM]<br>val [value](value.html): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Value of the lab value |

