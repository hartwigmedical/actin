package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.TreatmentHistoryEntryStartDateComparator
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadTreatmentWithCategoryOfTypesAsMostRecent(
    private val category: TreatmentCategory, private val type: TreatmentType?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorAntiCancerDrugs = record.clinical().oncologicalHistory()
            .filter {
                it.categories().any { category ->
                    TreatmentCategory.cancerTreatmentSet
                        .contains(category)
                }
            }

        val typeMatch = if (type != null) {
            priorAntiCancerDrugs
                .filter { it.isOfType(type) == true }
        } else {
            priorAntiCancerDrugs.filter { it.categories().contains(category) }
        }

        val mostRecentAntiCancerDrug = priorAntiCancerDrugs.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())

        return when {
            priorAntiCancerDrugs.isEmpty() -> {
                EvaluationFactory.recoverableFail(
                    "Patient has not received any prior anti cancer drugs",
                    "Has not received prior anti cancer drugs"
                )
            }

            type != null && mostRecentAntiCancerDrug?.isOfType(type) == true -> {
                EvaluationFactory.recoverablePass(
                    "Patient has received ${type.display()} ${category.display()} as the most recent treatment line",
                    "Has received ${type.display()} ${category.display()} as most recent treatment line"
                )
            }

            type == null && mostRecentAntiCancerDrug?.categories()?.contains(category) == true -> {
                EvaluationFactory.recoverablePass(
                    "Patient has received ${category.display()} as the most recent treatment line",
                    "Has received ${category.display()} as most recent treatment line"
                )
            }

            typeMatch.any { it.startYear() == null } -> {
                EvaluationFactory.undetermined(
                    "Has received${
                        type?.let { " ${it.display()}" }.orEmpty()
                    } ${category.display()} but undetermined if most recent (dates missing in treatment list)",
                    "Has received${type?.let { " ${it.display()}" }.orEmpty()} ${category.display()} but undetermined if most recent"
                )
            }

            typeMatch.isNotEmpty() -> {
                EvaluationFactory.recoverableFail(
                    "Patient has received${
                        type?.let { " ${it.display()}" }.orEmpty()
                    } ${category.display()} but not as the most recent treatment line",
                    "Has received${type?.let { " ${it.display()}" }.orEmpty()} ${category.display()} but not as most recent treatment line"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has not received${
                        type?.let { " ${it.display()}" }.orEmpty()
                    }  ${category.display()} as prior therapy",
                    "Has not received $type ${category.display()} as prior therapy"
                )
            }
        }
    }
}