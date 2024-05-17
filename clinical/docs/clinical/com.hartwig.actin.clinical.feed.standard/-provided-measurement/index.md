---
title: ProvidedMeasurement
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[ProvidedMeasurement](index.html)



# ProvidedMeasurement



[JVM]\
data class [ProvidedMeasurement](index.html)(val date: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val subcategory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

Data class representing a measurement in the EHR



## Constructors


| | |
|---|---|
| [ProvidedMeasurement](-provided-measurement.html) | [JVM]<br>constructor(date: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), subcategory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [category](category.html) | [JVM]<br>val [category](category.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Category of the measurement |
| [date](date.html) | [JVM]<br>val [date](date.html): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Date of the measurement |
| [subcategory](subcategory.html) | [JVM]<br>val [subcategory](subcategory.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Subcategory of the measurement |
| [unit](unit.html) | [JVM]<br>val [unit](unit.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Unit of the measurement |
| [value](value.html) | [JVM]<br>val [value](value.html): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Value of the measurement |

