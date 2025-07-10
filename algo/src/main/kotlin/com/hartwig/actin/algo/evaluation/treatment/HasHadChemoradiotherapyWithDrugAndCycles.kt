package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug

class HasHadChemoradiotherapyWithDrugAndCycles(private val drugs: Set<Drug>, private val cycles: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined("Undetermined if received chemoradiotherapy with ${Format.concatItemsWithOr(drugs)} and at least $cycles cycles")
    }
}