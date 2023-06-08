package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

internal object TreatmentTypeResolver {
    fun isOfType(
        treatment: PriorTumorTreatment, category: TreatmentCategory,
        typeToFind: String
    ): Boolean {
        val type = resolveType(treatment, category)
        return type != null && type.lowercase().contains(typeToFind.lowercase())
    }

    fun hasTypeConfigured(treatment: PriorTumorTreatment, category: TreatmentCategory): Boolean {
        val type = resolveType(treatment, category)
        return !type.isNullOrEmpty()
    }

    private fun resolveType(treatment: PriorTumorTreatment, category: TreatmentCategory): String? {
        return when (category) {
            TreatmentCategory.CHEMOTHERAPY -> {
                treatment.chemoType()
            }

            TreatmentCategory.IMMUNOTHERAPY -> {
                treatment.immunoType()
            }

            TreatmentCategory.TARGETED_THERAPY -> {
                treatment.targetedType()
            }

            TreatmentCategory.HORMONE_THERAPY -> {
                treatment.hormoneType()
            }

            TreatmentCategory.RADIOTHERAPY -> {
                treatment.radioType()
            }

            TreatmentCategory.TRANSPLANTATION -> {
                treatment.transplantType()
            }

            TreatmentCategory.SUPPORTIVE_TREATMENT -> {
                treatment.supportiveType()
            }

            TreatmentCategory.CAR_T -> {
                treatment.carTType()
            }

            TreatmentCategory.TRIAL -> {
                treatment.trialAcronym()
            }

            TreatmentCategory.ABLATION -> {
                treatment.ablationType()
            }

            else -> {
                null
            }
        }
    }
}