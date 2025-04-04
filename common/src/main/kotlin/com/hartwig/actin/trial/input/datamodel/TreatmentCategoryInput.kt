package com.hartwig.actin.trial.input.datamodel

import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.RadiotherapyType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class TreatmentCategoryInput(val mappedCategory: TreatmentCategory, val mappedType: TreatmentType? = null) : Displayable {

    override fun display(): String {
        return this.toString().replace("_".toRegex(), " ").lowercase()
    }

    companion object {

        fun fromString(input: String): TreatmentCategoryInput {
            val query: String = inputToEnumString(input)
            try {
                return TreatmentCategoryInput(TreatmentCategory.valueOf(query))
            } catch (e: IllegalArgumentException) {
            }
            val treatmentType: TreatmentType = resolveTreatmentType(query)
            return TreatmentCategoryInput(treatmentType.category, treatmentType)
        }

        fun treatmentTypeFromString(input: String): TreatmentType {
            return resolveTreatmentType(inputToEnumString(input))
        }

        private fun resolveTreatmentType(query: String): TreatmentType {
            val type =
                listOf(DrugType::valueOf, RadiotherapyType::valueOf, OtherTreatmentType::valueOf).firstNotNullOfOrNull { createType ->
                    try {
                        createType(query)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
            return type ?: throw IllegalArgumentException("Could not resolve string to a treatment category or type: $query")
        }

        private fun inputToEnumString(input: String): String {
            return input.trim { it <= ' ' }.replace(" ".toRegex(), "_").uppercase()
        }
    }
}