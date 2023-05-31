package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import com.hartwig.actin.util.ApplicationConfig

class HasHadAdjuvantTreatmentWithCategory(private val category: TreatmentCategory, private val types: Set<String>?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val adjuvantTreatmentsMatchingCategory = record.clinical().priorTumorTreatments().filter { it.categories().contains(category) }
            .filter { it.name().lowercase(ApplicationConfig.LOCALE).replace("neoadjuvant", "").contains("adjuvant") }

        val specificMatchingTreatmentNames =
            adjuvantTreatmentsMatchingCategory.filter { treatment -> treatmentHasValidType(treatment) }.map { it.name() }

        val generalMatchingTreatmentNames =
            adjuvantTreatmentsMatchingCategory.filter { !TreatmentTypeResolver.hasTypeConfigured(it, category) }.map { it.name() }

        val typesString = if (types == null) "" else " (${types.joinToString(", ")})"
        return when {
            specificMatchingTreatmentNames.isNotEmpty() -> EvaluationFactory.pass(
                "Patient has received adjuvant treatment(s) of category ${category.display()}$typesString: ${
                    Format.concat(
                        specificMatchingTreatmentNames
                    )
                }",
                "Has received adjuvant treatment(s) of ${category.display()}$typesString: ${Format.concat(specificMatchingTreatmentNames)}"
            )

            generalMatchingTreatmentNames.isNotEmpty() -> EvaluationFactory.warn(
                "Patient has received adjuvant treatment(s) of ${category.display()} with unknown type: ${
                    Format.concat(
                        generalMatchingTreatmentNames
                    )
                }", "Has received adjuvant treatment(s) of ${category.display()} with unknown type: ${
                    Format.concat(
                        generalMatchingTreatmentNames
                    )
                }"
            )

            else -> EvaluationFactory.fail(
                "Patient has not received adjuvant treatment(s) of ${category.display()}$typesString",
                "Has not received adjuvant treatment(s) of ${category.display()}$typesString"
            )

        }
    }

    private fun treatmentHasValidType(treatment: PriorTumorTreatment): Boolean {
        return types == null || (TreatmentTypeResolver.hasTypeConfigured(treatment, category) && types.any {
            TreatmentTypeResolver.isOfType(
                treatment, category, it
            )
        })
    }
}