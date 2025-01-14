package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHistoryOfAnaphylaxis(private val icdModel: IcdModel): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val anaphylaxisCode = setOf(IcdCode(IcdConstants.ANAPHYLAXIS_CODE))
        val anaphylaxisEntries = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, anaphylaxisCode).fullMatches

        return if (anaphylaxisEntries.isNotEmpty()) {
            EvaluationFactory.pass("Has history of anaphylaxis: ${Format.concatItemsWithAnd(anaphylaxisEntries)}")
        } else {
            EvaluationFactory.fail("No known history of anaphylaxis")
        }
    }
}