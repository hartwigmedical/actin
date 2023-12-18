package com.hartwig.actin.treatment.input.datamodel

import com.hartwig.actin.Displayable
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.RadiotherapyType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.function.Function

class TreatmentCategoryInput private constructor(
    private val mappedCategory: TreatmentCategory,
    private val mappedType: TreatmentType? = null
) : Displayable {
    fun mappedCategory(): TreatmentCategory {
        return mappedCategory
    }

    fun mappedType(): TreatmentType? {
        return mappedType
    }

    public override fun display(): String {
        return this.toString().replace("_".toRegex(), " ").lowercase(Locale.getDefault())
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(TreatmentCategoryInput::class.java)

        @JvmStatic
        fun fromString(input: String): TreatmentCategoryInput {
            val query: String = inputToEnumString(input)
            try {
                return TreatmentCategoryInput(TreatmentCategory.valueOf(query))
            } catch (e: IllegalArgumentException) {
                LOGGER.debug("Treatment category not found for query string {}", query)
            }
            val treatmentType: TreatmentType = resolveTreatmentType(query)
            return TreatmentCategoryInput(treatmentType.category(), treatmentType)
        }

        @JvmStatic
        fun treatmentTypeFromString(input: String): TreatmentType {
            return resolveTreatmentType(inputToEnumString(input))
        }

        private fun resolveTreatmentType(query: String): TreatmentType {
            val typeCreators: List<Function<String, TreatmentType>> = java.util.List.of(
                Function({ name: String? -> DrugType.valueOf((name)!!) }), Function({ name: String? ->
                    RadiotherapyType.valueOf(
                        (name)!!
                    )
                }), Function({ name: String? -> OtherTreatmentType.valueOf((name)!!) })
            )
            for (createType: Function<String, TreatmentType> in typeCreators) {
                try {
                    return createType.apply(query)
                } catch (e: IllegalArgumentException) {
                    LOGGER.debug("Type not found for query string {}", query)
                }
            }
            throw IllegalArgumentException("Could not resolve string to a treatment category or type: " + query)
        }

        private fun inputToEnumString(input: String): String {
            return input.trim({ it <= ' ' }).replace(" ".toRegex(), "_").uppercase(Locale.getDefault())
        }
    }
}
