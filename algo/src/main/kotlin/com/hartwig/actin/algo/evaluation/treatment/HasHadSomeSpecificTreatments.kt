package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentCategory

class HasHadSomeSpecificTreatments internal constructor(
    private val names: Set<String>, private val warnCategory: TreatmentCategory?,
    private val minTreatmentLines: Int
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val warnCategories = setOf(TreatmentCategory.TRIAL, warnCategory).filterNotNull()
        val warnTreatments = record.clinical().priorTumorTreatments()
            .filter { treatment: PriorTumorTreatment -> warnCategories.any { treatment.categories().contains(it) } }
            .map { it.name() }

        val matchTreatments = record.clinical().priorTumorTreatments()
            .map { it.name() }
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it, names) }

        return if (matchTreatments.size >= minTreatmentLines) {
            EvaluationFactory.pass(
                "Patient has received " + concat(matchTreatments) + " " + matchTreatments.size + " times",
                "Has received " + concat(matchTreatments) + " " + matchTreatments.size + " times"
            )
        } else if (warnTreatments.size >= minTreatmentLines) {
            val undeterminedSpecificMessage =
                if (warnCategory != null) ("Patient has received " + warnCategory.display() + " or trial treatment " + warnTreatments.size
                        + " times") else "Patient has received " + concat(warnTreatments) + " treatments including trials"
            val undeterminedGeneralMessage =
                if (warnCategory != null) "Received " + warnCategory.display() + " or trials " + warnTreatments.size + " times" else "Received " + concat(
                    warnTreatments
                ) + " treatments including trials"
            EvaluationFactory.undetermined(undeterminedSpecificMessage, undeterminedGeneralMessage)
        } else {
            EvaluationFactory.fail(
                "Patient has not received " + concat(names) + " at least " + minTreatmentLines + " times",
                "Has not received " + concat(names) + " at least " + minTreatmentLines + " times"
            )
        }
    }
}