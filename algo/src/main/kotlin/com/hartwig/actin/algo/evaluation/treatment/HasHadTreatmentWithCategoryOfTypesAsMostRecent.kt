package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadTreatmentWithCategoryOfTypesAsMostRecent(
    private val category: TreatmentCategory, private val type: TreatmentType?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorAntiCancerDrugs = record.clinical().oncologicalHistory()
            .filter {
                it.categories().any { category ->
                    TreatmentCategory.CANCER_TREATMENT_CATEGORIES
                        .contains(category)
                }
            }

        val treatmentMatch = if (type != null) {
            priorAntiCancerDrugs
                .filter { it.isOfType(type) == true }
        } else {
            priorAntiCancerDrugs.filter { it.categories().contains(category) }
        }

        val mostRecentAntiCancerDrug = priorAntiCancerDrugs.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())
        val nullableTypeString = type?.let { " ${it.display()}" }.orEmpty()

        return when {
            priorAntiCancerDrugs.isEmpty() -> {
                EvaluationFactory.fail(
                    "Patient has not received any prior anti cancer drugs",
                    "Has not received prior anti cancer drugs"
                )
            }

            type != null && mostRecentAntiCancerDrug?.isOfType(type) == true -> {
                EvaluationFactory.pass(
                    "Patient has received ${type.display()} ${category.display()} as the most recent treatment line",
                    "Has received ${type.display()} ${category.display()} as most recent treatment line"
                )
            }

            type == null && mostRecentAntiCancerDrug?.categories()?.contains(category) == true -> {
                EvaluationFactory.pass(
                    "Patient has received ${category.display()} as the most recent treatment line",
                    "Has received ${category.display()} as most recent treatment line"
                )
            }

            treatmentMatch.any { it.startYear() == null } -> {
                EvaluationFactory.undetermined(
                    "Has received$nullableTypeString ${category.display()} but undetermined if most recent (dates missing in treatment list)",
                    "Has received$nullableTypeString ${category.display()} but undetermined if most recent"
                )
            }

            treatmentMatch.isNotEmpty() -> {
                EvaluationFactory.fail(
                    "Patient has received$nullableTypeString ${category.display()} but not as the most recent treatment line",
                    "Has received$nullableTypeString ${category.display()} but not as most recent treatment line"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received$nullableTypeString ${category.display()} as prior therapy",
                    "Has not received$nullableTypeString ${category.display()} as prior therapy"
                )
            }
        }
    }
}