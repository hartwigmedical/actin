package com.hartwig.actin.clinical.curation

object CurationUtil {

    private const val IGNORE = "<ignore>"
    private const val DELIMITER = ";"

    fun isIgnoreString(input: String) = input == IGNORE
    
    fun capitalizeFirstLetterOnly(string: String?): String =
        string?.takeIf { it.isNotEmpty() }
            ?.let { it.substring(0, 1).uppercase() + it.substring(1).lowercase() }
            ?: ""

    fun fullTrim(input: String): String {
        var reformatted = input.trim { it <= ' ' }
        while (reformatted.contains("  ")) {
            reformatted = reformatted.replace(" {2}".toRegex(), " ")
        }
        return reformatted
    }

    fun toDOIDs(doidString: String): Set<String> {
        return toSet(doidString)
    }

    fun toCategories(categoriesString: String): Set<String> {
        return toSet(categoriesString)
    }

    fun toIcdTitles(icdTitlesString: String): Set<String> {
        return toSet(icdTitlesString)
    }

    fun toSet(setString: String): Set<String> {
        return setString.split(DELIMITER).map { it.trim() }.filterNot { it.isEmpty() }.toSet()
    }
}