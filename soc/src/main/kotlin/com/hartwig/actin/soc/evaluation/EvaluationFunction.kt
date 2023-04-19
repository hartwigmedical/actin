package com.hartwig.actin.soc.evaluation

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation

interface EvaluationFunction {

    fun evaluate(record: PatientRecord): Evaluation
}