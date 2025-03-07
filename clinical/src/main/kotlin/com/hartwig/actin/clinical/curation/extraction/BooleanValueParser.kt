package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.util.Either
import com.hartwig.actin.util.left
import com.hartwig.actin.util.right

object BooleanValueParser {

    private val OPTION_MAPPING = mapOf(
        "no" to false,
        "non" to false,
        "none" to false,
        "no indien ja welke" to false,
        "nee" to false,
        "neee" to false,
        "o" to false,
        "n.v.t." to false,
        "n.v.t" to false,
        "nvt" to false,
        "nvt." to false,
        "na" to null,
        "yes" to true,
        "tes" to true,
        "ja" to true,
        "es" to true,
        "yes/no" to null,
        "yes/no/unknown" to null,
        "(yes/no)" to null,
        "ye" to true,
        "yes related to prostatecarcinoma" to true,
        "yes (cervix)" to true,
        "yes bone lesion l1 l2 with epidural extension" to true,
        "yes manubrium sterni" to true,
        "yes vertebra l2" to true,
        "yes wherefore surgery jun 2023" to true,
        "unknown" to null,
        "uknown" to null,
        "unknonw" to null,
        "onknown" to null,
        "unkown" to null,
        "unknown (can be seem on ct but not clearly demarcated, has not been measured before)" to null,
        "suspect lesion" to null,
        "unknown after surgery" to null,
        "-" to null,
        "botaantasting bij weke delen massa" to false,
        "no total resection" to false,
        "probably" to null,
        "possible" to null,
        "onbekend" to null,
        "suspected" to null
    )

    fun parseBoolean(input: String?): Either<String, Boolean?> {
        val key = input?.lowercase()
        return if (key.isNullOrEmpty() || OPTION_MAPPING.containsKey(key)) OPTION_MAPPING[key].right() else key.left()
    }

    fun isTrue(input: String?): Boolean {
        return parseBoolean(input) == Either.Right(true)
    }

    fun isUnknown(input: String?): Boolean {
        return parseBoolean(input) == Either.Right(null)
    }
}