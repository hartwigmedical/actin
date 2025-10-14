package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class MmrStatusIsAvailable(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcResultAvailable = IhcTestEvaluation.create("MSI", record.ihcTests).filteredTests.isNotEmpty()
        val molecularResultAvailable = record.molecularTests.any { it.characteristics.microsatelliteStability != null }

        return if (ihcResultAvailable || molecularResultAvailable) {
            EvaluationFactory.pass("MMR status is available")
        } else {
            EvaluationFactory.recoverableFail("No MMR status available")
        }
    }
}