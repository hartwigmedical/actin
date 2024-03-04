package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

object TreatmentCategoryResolver {

    private const val DELIMITER = ", "

    fun fromStringList(categoryStringList: String): Set<TreatmentCategory> {
        return categoryStringList.split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.map(::fromString).toSet()
    }

    fun fromString(categoryString: String): TreatmentCategory {
        return TreatmentCategory.valueOf(categoryString.trim { it <= ' ' }.replace(" ".toRegex(), "_").uppercase())
    }

    fun toStringList(categories: Set<TreatmentCategory>): String {
        return categories.joinToString(DELIMITER) { toString(it) }
    }

    fun toString(category: TreatmentCategory): String {
        val string = category.toString().replace("_".toRegex(), " ")
        return string.substring(0, 1).uppercase() + string.substring(1).lowercase()
    }
}
