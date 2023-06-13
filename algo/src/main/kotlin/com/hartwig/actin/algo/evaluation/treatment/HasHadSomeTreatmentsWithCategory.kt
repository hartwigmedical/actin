package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadSomeTreatmentsWithCategory internal constructor(private val category: TreatmentCategory, private val minTreatmentLines: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var numTreatmentLines = 0
        var numOtherTrials = 0
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                numOtherTrials++
            }
        }
        return if (numTreatmentLines >= minTreatmentLines) {
            EvaluationFactory.pass(
                "Patient has received at least $minTreatmentLines line(s) of ${category.display()}",
                "Has received at least $minTreatmentLines line(s) of ${category.display()}"
            )
        } else if (numTreatmentLines + numOtherTrials >= minTreatmentLines) {
            EvaluationFactory.undetermined(
                "Patient may have received at least $minTreatmentLines line(s) of  ${category.display()}",
                "Undetermined if received at least $minTreatmentLines line(s) of ${category.display()}"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received at least $minTreatmentLines line(s) of ${category.display()}",
                "Has not received at least $minTreatmentLines line(s) of ${category.display()}"
            )
        }
    }
}