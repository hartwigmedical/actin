package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadLimitedTreatmentsWithCategoryOfTypes(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>,
    private val maxTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, category, { historyEntry -> historyEntry.matchesTypeFromSet(types) }
        )

        val typesList = concatItems(types)
        return when {
            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches <= maxTreatmentLines ->
                EvaluationFactory.pass("Has received at most $maxTreatmentLines lines of $typesList ${category.display()}")

            treatmentSummary.numSpecificMatches() <= maxTreatmentLines ->
                EvaluationFactory.undetermined("Unclear if has received at most $maxTreatmentLines lines of $typesList ${category.display()}")

            else ->
                EvaluationFactory.fail("Has exceeded $maxTreatmentLines lines of $typesList ${category.display()}")
        }
    }
}