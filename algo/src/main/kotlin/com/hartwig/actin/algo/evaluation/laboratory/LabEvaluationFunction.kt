package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.clinical.datamodel.LabValue

interface LabEvaluationFunction {
    fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation
}