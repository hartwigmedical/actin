package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class CurrentlyGetsQTProlongatingMedication internal constructor(private val selector: MedicationSelector) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val activeMedications = selector.active(record.clinical().medications())
        val qtMedication = activeMedications.groupBy({ it.qtProlongatingRisk() }, { it.name() }).filterKeys { it != QTProlongatingRisk.NONE }
        return if (qtMedication.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets potential QT prolongating medication: " + concatWithType(qtMedication),
                "QT prolongating medication use: " + concatWithType(qtMedication)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get QT prolongating medication ",
                "No QT prolongating medication use "
            )
        }
    }

    private fun concatWithType(medications: Map<QTProlongatingRisk, List<String>>): String {
        val joinedValues = ArrayList<String>()
        for (key in medications.keys) {
            joinedValues.add(medications[key]!!.joinToString { " ($key) and " })
        }
        return joinedValues.joinToString { " and " }
    }
}