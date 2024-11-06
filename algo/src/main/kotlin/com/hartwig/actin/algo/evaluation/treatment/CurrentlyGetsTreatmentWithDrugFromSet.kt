package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug

class CurrentlyGetsTreatmentWithDrugFromSet(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

    }
}