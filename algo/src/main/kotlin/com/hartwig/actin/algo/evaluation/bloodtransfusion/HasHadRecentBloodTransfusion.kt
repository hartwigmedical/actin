package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class HasHadRecentBloodTransfusion(private val product: TransfusionProduct, private val minDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val productString = product.display().lowercase()
        for (transfusion in record.bloodTransfusions) {
            if (transfusion.product.equals(product.display(), ignoreCase = true) && minDate.isBefore(transfusion.date)) {
                return EvaluationFactory.pass("Has received recent $productString blood transfusion")
            }
        }
        return EvaluationFactory.fail("Has not received recent $productString blood transfusion")
    }
}