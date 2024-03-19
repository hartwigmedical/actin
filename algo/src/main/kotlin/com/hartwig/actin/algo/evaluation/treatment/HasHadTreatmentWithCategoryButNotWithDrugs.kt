package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadTreatmentWithCategoryButNotWithDrugs(
    private val category: TreatmentCategory,
    private val ignoreDrugs: Set<Drug>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category) { entry ->
                entry.allTreatments().none { (it as? DrugTreatment)?.drugs?.intersect(ignoreDrugs)?.isNotEmpty() == true }
            }

        val ignoreDrugsList = concatItemsWithAnd(ignoreDrugs)
        return when {
            treatmentSummary.hasSpecificMatch() -> EvaluationFactory.pass("Has received ${category.display()} ignoring $ignoreDrugsList")

            treatmentSummary.hasPossibleTrialMatch() -> EvaluationFactory.undetermined(
                "Patient may have received ${category.display()} ignoring $ignoreDrugsList due to trial participation",
                "Undetermined if received ${category.display()} ignoring $ignoreDrugsList due to trial participation"
            )

            else -> EvaluationFactory.fail("Has not received ${category.display()} ignoring $ignoreDrugsList")
        }
    }
}