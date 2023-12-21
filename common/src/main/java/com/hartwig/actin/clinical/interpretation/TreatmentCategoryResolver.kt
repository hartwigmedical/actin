package com.hartwig.actin.clinical.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import java.util.*

object TreatmentCategoryResolver {
    private const val DELIMITER = ", "

    @JvmStatic
    fun fromStringList(categoryStringList: String): Set<TreatmentCategory> {
        val categories: MutableSet<TreatmentCategory> = Sets.newTreeSet()
        for (categoryString in categoryStringList.split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            categories.add(fromString(categoryString))
        }
        return categories
    }

    fun fromString(categoryString: String): TreatmentCategory {
        return TreatmentCategory.valueOf(categoryString.trim { it <= ' ' }.replace(" ".toRegex(), "_").uppercase(Locale.getDefault()))
    }

    fun toStringList(categories: Set<TreatmentCategory>): String {
        val joiner = StringJoiner(DELIMITER)
        for (category in categories) {
            joiner.add(toString(category))
        }
        return joiner.toString()
    }

    fun toString(category: TreatmentCategory): String {
        val string = category.toString().replace("_".toRegex(), " ")
        return string.substring(0, 1).uppercase(Locale.getDefault()) + string.substring(1).lowercase(Locale.getDefault())
    }
}
