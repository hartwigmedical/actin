package com.hartwig.actin.clinical.curation

import com.google.common.collect.Sets

object CurationUtil {
    private const val IGNORE = "<ignore>"
    private const val DOID_DELIMITER = ";"
    private const val CATEGORIES_DELIMITER = ";"
    fun isIgnoreString(input: String): Boolean {
        return input == IGNORE
    }

    fun capitalizeFirstLetterOnly(string: String): String {
        return if (string.isEmpty()) {
            string
        } else string.substring(0, 1).uppercase() + string.substring(1).lowercase()
    }

    fun fullTrim(input: String): String {
        var reformatted = input.trim { it <= ' ' }
        while (reformatted.contains("  ")) {
            reformatted = reformatted.replace(" {2}".toRegex(), " ")
        }
        return reformatted
    }

    fun toDOIDs(doidString: String): Set<String> {
        return toSet(doidString, DOID_DELIMITER)
    }

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