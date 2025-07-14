package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPlatinumSensitive(private val referenceDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumFunctions = PlatinumFunctions.create(record)

        return when {
            platinumFunctions.hasProgressionOnPlatinumWithinSixMonths(referenceDate) -> EvaluationFactory.fail("Is platinum resistant")
            platinumFunctions.hasProgressionOrUnknownProgressionOnPlatinum() -> EvaluationFactory.undetermined("Undetermined if patient is platinum sensitive")
            platinumFunctions.platinumTreatments.isNotEmpty() -> EvaluationFactory.fail("Not platinum sensitive (no progression on platinum treatment)")
            else -> EvaluationFactory.fail("Not platinum sensitive (no platinum treatment)")
        }
    }
}