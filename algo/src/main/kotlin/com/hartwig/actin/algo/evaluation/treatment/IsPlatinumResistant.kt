package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPlatinumResistant(private val referenceDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumFunctions = PlatinumFunctions.create(record)

        return when {
            platinumFunctions.hasProgressionOnPlatinumWithinSixMonths(referenceDate) -> EvaluationFactory.pass("Is platinum resistant")
            platinumFunctions.hasProgressionOrUnknownProgressionOnPlatinum() -> EvaluationFactory.undetermined("Undetermined if patient is platinum resistant")
            platinumFunctions.platinumTreatments.isNotEmpty() -> EvaluationFactory.fail("Not platinum resistant (no progression on platinum treatment)")
            else -> EvaluationFactory.fail("Not platinum resistant (no platinum treatment)")
        }
    }
}