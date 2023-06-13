package com.hartwig.actin.clinical.curation

import com.google.common.collect.Sets
import java.util.*

object CurationUtil {
    private const val IGNORE = "<ignore>"
    private const val DOID_DELIMITER = ";"
    private const val CATEGORIES_DELIMITER = ";"
    fun isIgnoreString(input: String): Boolean {
        return input == IGNORE
    }

    @JvmStatic
    fun capitalizeFirstLetterOnly(string: String): String {
        return if (string.isEmpty()) {
            string
        } else string.substring(0, 1).uppercase(Locale.getDefault()) + string.substring(1).lowercase(Locale.getDefault())
    }

    @JvmStatic
    fun fullTrim(input: String): String {
        var reformatted = input.trim { it <= ' ' }
        while (reformatted.contains("  ")) {
            reformatted = reformatted.replace(" {2}".toRegex(), " ")
        }
        return reformatted
    }

    @JvmStatic
    fun toDOIDs(doidString: String): Set<String> {
        return toSet(doidString, DOID_DELIMITER)
    }

    @JvmStatic
    fun toCategories(categoriesString: String): Set<String> {
        return toSet(categoriesString, CATEGORIES_DELIMITER)
    }

    private fun toSet(setString: String, delimiter: String): Set<String> {
        if (setString.isEmpty()) {
            return Sets.newHashSet()
        }
        val strings: MutableSet<String> = Sets.newHashSet()
        for (string in setString.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            strings.add(string.trim { it <= ' ' })
        }
        return strings
    }
}