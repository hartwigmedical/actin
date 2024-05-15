//[clinical](../../../index.md)/[com.hartwig.actin.clinical.feed.standard](../index.md)/[EhrMeasurement](index.md)

# EhrMeasurement

[JVM]\
data class [EhrMeasurement](index.md)(val date: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), val category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val subcategory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

Data class representing a measurement in the EHR

## Constructors

| | |
|---|---|
| [EhrMeasurement](-ehr-measurement.md) | [JVM]<br>constructor(date: [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), category: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), subcategory: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, value: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), unit: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [category](category.md) | [JVM]<br>val [category](category.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Category of the measurement |
| [date](date.md) | [JVM]<br>val [date](date.md): [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)<br>Date of the measurement |
| [subcategory](subcategory.md) | [JVM]<br>val [subcategory](subcategory.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Subcategory of the measurement |
| [unit](unit.md) | [JVM]<br>val [unit](unit.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Unit of the measurement |
| [value](value.md) | [JVM]<br>val [value](value.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Value of the measurement |
