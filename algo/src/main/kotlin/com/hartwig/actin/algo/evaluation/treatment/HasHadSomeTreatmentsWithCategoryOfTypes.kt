package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadSomeTreatmentsWithCategoryOfTypes(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>, private val minTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category) {
            it.matchesTypeFromSet(types)
        }
        val typesList = concatItems(types)
        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass("Has received at least $minTreatmentLines line(s) of $typesList ${category.display()}")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    "Can't determine whether patient has received at least $minTreatmentLines line(s) of $typesList ${category.display()}",
                    "Undetermined if received at least $minTreatmentLines line(s) of $typesList ${category.display()}"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minTreatmentLines line(s) of $typesList ${category.display()}")
            }
        }
    }
}