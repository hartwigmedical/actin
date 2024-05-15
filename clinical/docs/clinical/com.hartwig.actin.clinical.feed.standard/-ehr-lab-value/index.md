//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[EhrLabValue](index.md)

# EhrLabValue

[JVM]\
data class [EhrLabValue](index.md)(val evaluationTime: [LocalDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html), val measure: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val measureCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val refUpperBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val refLowerBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val comparator: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val refFlag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

Data class representing a lab value in the EHR

## Constructors

| | |
|---|---|
| [EhrLabValue](-ehr-lab-value.md) | [JVM]<br>constructor(evaluationTime: [LocalDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html), measure: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), measureCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, refUpperBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), refLowerBound: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), comparator: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, refFlag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [comparator](comparator.md) | [JVM]<br>val [comparator](comparator.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Comparator of the lab value |
| [evaluationTime](evaluation-time.md) | [JVM]<br>val [evaluationTime](evaluation-time.md): [LocalDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html)<br>Evaluation time of the lab value |
| [measure](measure.md) | [JVM]<br>val [measure](measure.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Measure of the lab value |
| [measureCode](measure-code.md) | [JVM]<br>val [measureCode](measure-code.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Measure code of the lab value |
| [refFlag](ref-flag.md) | [JVM]<br>val [refFlag](ref-flag.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Reference flag of the lab value |
| [refLowerBound](ref-lower-bound.md) | [JVM]<br>val [refLowerBound](ref-lower-bound.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Reference lower bound of the lab value |
| [refUpperBound](ref-upper-bound.md) | [JVM]<br>val [refUpperBound](ref-upper-bound.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Reference upper bound of the lab value |
| [unit](unit.md) | [JVM]<br>val [unit](unit.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Unit of the lab value |
| [value](value.md) | [JVM]<br>val [value](value.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Value of the lab value |
