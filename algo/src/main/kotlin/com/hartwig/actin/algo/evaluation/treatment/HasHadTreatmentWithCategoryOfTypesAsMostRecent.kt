package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
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
    private val category: TreatmentCategory, private val types: Set<TreatmentType>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
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

        val typeCategoryMatches = TreatmentSummaryForCategory.createForTreatmentHistory(record.clinical().oncologicalHistory(), category) {
            it.matchesTypeFromSet(types)
        }

        val typesList = concatItems(types)

        return when {
            typeCategoryMatches.specificMatches.any { it.categories() == mostRecentAntiCancerDrug } -> {
                EvaluationFactory.recoverablePass(
                    "Patient has received $typesList ${category.display()} as the most recent treatment line",
                    "Has received $typesList ${category.display()} as most recent treatment line"
                )
            }

            typeCategoryMatches.specificMatches.isNotEmpty() && typeCategoryMatches.specificMatches.none { it.categories() == mostRecentAntiCancerDrug } -> {
                EvaluationFactory.recoverableFail(
                    "Patient has received $typesList ${category.display()} but not as the most recent treatment line",
                    "Has received $typesList ${category.display()} but not as most recent treatment line"
                )
            }

            /*
            TO DO
            recoverableUndetermined if startdate of specific match is unknown
             */

            typeCategoryMatches.numSpecificMatches() + typeCategoryMatches.numApproximateMatches + typeCategoryMatches.numPossibleTrialMatches >= 1 -> {
                EvaluationFactory.undetermined(
                    "Can't determine whether patient has received $typesList ${category.display()} as most recent line ",
                    "Undetermined if received $typesList ${category.display()} as most recent line"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has not received $typesList ${category.display()} as most recent treatment line",
                    "Has not received $typesList ${category.display()} as most recent treatment"
                )
            }
        }
    }
}