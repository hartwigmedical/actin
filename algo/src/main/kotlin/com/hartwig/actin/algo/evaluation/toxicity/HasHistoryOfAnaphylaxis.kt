package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.Intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.evaluation.othercondition.PriorOtherConditionFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHistoryOfAnaphylaxis(private val icdModel: IcdModel): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val anaphylaxisCode = setOf(IcdCode(IcdConstants.ANAPHYLAXIS_CODE))
        val anaphylaxisHistoryEntries = PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
            icdModel, record, anaphylaxisCode).fullMatches

        val anaphylaxisIntoleranceEntries =
            IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(icdModel, record, anaphylaxisCode).fullMatches

        val conditionString = Format.concatWithCommaAndAnd((anaphylaxisIntoleranceEntries + anaphylaxisIntoleranceEntries).map { it.name })

        return if (anaphylaxisIntoleranceEntries.isNotEmpty() || anaphylaxisHistoryEntries.isNotEmpty()) {
            EvaluationFactory.pass("Has history of anaphylaxis: $conditionString")
        } else {
            EvaluationFactory.fail("No history of anaphylaxis")
        }
    }
}