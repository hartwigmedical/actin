package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

interface EvaluationFunction {

    fun evaluate(record: PatientRecord): Evaluation
}