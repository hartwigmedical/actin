package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

//TODO: Implement Treatment history > Treatment with radioType like '%TACE%' or '%TARE%' and date within X weeks. `UNDETERMINED` In case of Radiotherapy treatment without type or date specified
class HasHadLocalHepaticTherapyWithinWeeks() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined("Undetermined if patient has had local hepatic therapy within specified time frame")
    }
}