---
title: RemoveNewlinesAndCarriageReturns
---
//[clinical](../../../index.html)/[com.hartwig.actin.clinical.feed.standard](../index.html)/[RemoveNewlinesAndCarriageReturns](index.html)



# RemoveNewlinesAndCarriageReturns



[JVM]\
class [RemoveNewlinesAndCarriageReturns](index.html) : JsonDeserializer&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;



## Constructors


| | |
|---|---|
| [RemoveNewlinesAndCarriageReturns](-remove-newlines-and-carriage-returns.html) | [JVM]<br>constructor() |


## Functions


| Name | Summary |
|---|---|
| [deserialize](index.html#-1701658741%2FFunctions%2F1757943785) | [JVM]<br>open fun [deserialize](index.html#-1701658741%2FFunctions%2F1757943785)(p0: JsonParser, p1: DeserializationContext, p2: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>open override fun [deserialize](deserialize.html)(p0: JsonParser, p1: DeserializationContext?): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [deserializeWithType](index.html#303597567%2FFunctions%2F1757943785) | [JVM]<br>open fun [deserializeWithType](index.html#303597567%2FFunctions%2F1757943785)(p0: JsonParser, p1: DeserializationContext, p2: TypeDeserializer): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)<br>open fun [deserializeWithType](index.html#1334083236%2FFunctions%2F1757943785)(p0: JsonParser, p1: DeserializationContext, p2: TypeDeserializer, p3: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html) |
| [findBackReference](index.html#1438700766%2FFunctions%2F1757943785) | [JVM]<br>open fun [findBackReference](index.html#1438700766%2FFunctions%2F1757943785)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): SettableBeanProperty |
| [getAbsentValue](index.html#-390729380%2FFunctions%2F1757943785) | [JVM]<br>open override fun [getAbsentValue](index.html#-390729380%2FFunctions%2F1757943785)(p0: DeserializationContext): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html) |
| [getDelegatee](index.html#-1050556161%2FFunctions%2F1757943785) | [JVM]<br>open fun [getDelegatee](index.html#-1050556161%2FFunctions%2F1757943785)(): JsonDeserializer&lt;*&gt; |
| [getEmptyAccessPattern](index.html#2004370652%2FFunctions%2F1757943785) | [JVM]<br>open fun [getEmptyAccessPattern](index.html#2004370652%2FFunctions%2F1757943785)(): AccessPattern |
| [getEmptyValue](index.html#2066120599%2FFunctions%2F1757943785) | [JVM]<br>open fun [~~getEmptyValue~~](index.html#2066120599%2FFunctions%2F1757943785)(): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)<br>open fun [getEmptyValue](index.html#-1621668596%2FFunctions%2F1757943785)(p0: DeserializationContext): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html) |
| [getKnownPropertyNames](index.html#808020811%2FFunctions%2F1757943785) | [JVM]<br>open fun [getKnownPropertyNames](index.html#808020811%2FFunctions%2F1757943785)(): [MutableCollection](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-collection/index.html)&lt;[Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; |
| [getNullAccessPattern](index.html#-96796966%2FFunctions%2F1757943785) | [JVM]<br>open override fun [getNullAccessPattern](index.html#-96796966%2FFunctions%2F1757943785)(): AccessPattern |
| [getNullValue](index.html#-1752557675%2FFunctions%2F1757943785) | [JVM]<br>open fun [~~getNullValue~~](index.html#-1752557675%2FFunctions%2F1757943785)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>open override fun [getNullValue](index.html#432781262%2FFunctions%2F1757943785)(p0: DeserializationContext): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getObjectIdReader](index.html#911426750%2FFunctions%2F1757943785) | [JVM]<br>open fun [getObjectIdReader](index.html#911426750%2FFunctions%2F1757943785)(): ObjectIdReader |
| [handledType](index.html#1063755675%2FFunctions%2F1757943785) | [JVM]<br>open fun [handledType](index.html#1063755675%2FFunctions%2F1757943785)(): [Class](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)&lt;*&gt; |
| [isCachable](index.html#1654902530%2FFunctions%2F1757943785) | [JVM]<br>open fun [isCachable](index.html#1654902530%2FFunctions%2F1757943785)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [logicalType](index.html#1638353390%2FFunctions%2F1757943785) | [JVM]<br>open fun [logicalType](index.html#1638353390%2FFunctions%2F1757943785)(): LogicalType |
| [replaceDelegatee](index.html#79105129%2FFunctions%2F1757943785) | [JVM]<br>open fun [replaceDelegatee](index.html#79105129%2FFunctions%2F1757943785)(p0: JsonDeserializer&lt;*&gt;): JsonDeserializer&lt;*&gt; |
| [supportsUpdate](index.html#336340330%2FFunctions%2F1757943785) | [JVM]<br>open fun [supportsUpdate](index.html#336340330%2FFunctions%2F1757943785)(p0: DeserializationConfig): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [unwrappingDeserializer](index.html#-1815728544%2FFunctions%2F1757943785) | [JVM]<br>open fun [unwrappingDeserializer](index.html#-1815728544%2FFunctions%2F1757943785)(p0: NameTransformer): JsonDeserializer&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt; |

