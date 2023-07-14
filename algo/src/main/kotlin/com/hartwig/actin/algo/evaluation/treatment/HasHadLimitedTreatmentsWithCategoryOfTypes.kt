package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadLimitedTreatmentsWithCategoryOfTypes(
    private val category: TreatmentCategory, private val types: List<String>,
    private val maxTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatmentHistory(record.clinical().treatmentHistory(), category) { treatment ->
                TreatmentTypeResolver.matchesTypeFromCollection(treatment, category, types)
            }

        return when {
            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches <= maxTreatmentLines ->
                EvaluationFactory.pass("Has received at most $maxTreatmentLines lines of ${concat(types)} ${category.display()}")

            treatmentSummary.numSpecificMatches() <= maxTreatmentLines ->
                EvaluationFactory.undetermined("Unclear if has received at most $maxTreatmentLines lines of ${concat(types)} ${category.display()}")

            else ->
                EvaluationFactory.fail("Has not received at most $maxTreatmentLines lines of ${concat(types)} ${category.display()}")
        }
    }
}