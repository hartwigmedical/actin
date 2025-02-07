package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue

interface LabEvaluationFunction {

    fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation
}