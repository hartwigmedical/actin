package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.CAR_T
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.CHEMOTHERAPY
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.GENE_THERAPY
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.HORMONE_THERAPY
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.IMMUNOTHERAPY
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.TARGETED_THERAPY
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.TCR_T
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory.TRIAL
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadTreatmentWithCategoryOfTypesAsMostRecent(
    private val category: TreatmentCategory, private val type: TreatmentType?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val typeMatch = record.clinical().oncologicalHistory()
            .filter { it.isOfType(type) ?: false }

        val mostRecentAntiCancerDrug = record.clinical().oncologicalHistory()
            .filter {
                it.categories().any { category ->
                    setOf(
                        CHEMOTHERAPY,
                        TARGETED_THERAPY,
                        IMMUNOTHERAPY,
                        HORMONE_THERAPY,
                        TRIAL,
                        CAR_T,
                        TCR_T,
                        GENE_THERAPY
                    )
                        .contains(category)
                }
            }.sortedBy { it.startYear() }
            .sortedBy { it.startMonth() }
            .takeLast(1)

        return when {
            typeMatch.any { it == mostRecentAntiCancerDrug.first() } -> {
                EvaluationFactory.recoverablePass(
                    "Patient has received $type ${category.display()} as the most recent treatment line",
                    "Has received $type ${category.display()} as most recent treatment line"
                )
            }

            typeMatch.isNotEmpty() && typeMatch.any { it.startYear() == null } -> {
                EvaluationFactory.undetermined(
                    "Has received ${type?.display()} ${category.display()} but undetermined if most recent (dates missing in treatment list)",
                    "Has received ${type?.display()} ${category.display()} but undetermined if most recent"
                )
            }

            typeMatch.isNotEmpty() && typeMatch.none { it == mostRecentAntiCancerDrug } -> {
                EvaluationFactory.recoverableFail(
                    "Patient has received ${type?.display()} ${category.display()} but not as the most recent treatment line",
                    "Has received ${type?.display()} ${category.display()} but not as most recent treatment line"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has not received $type ${category.display()} as prior therapy",
                    "Has not received $type ${category.display()} as prior therapy"
                )
            }
        }
    }
}