package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Assert.*
import org.junit.Test


class HasHadDefinitiveLocoregionalTherapyWithCurativeIntentTest {

    @Test
    fun `Should warn when treatment history is empty`() {
        assertEvaluation(
            EvaluationResult.WARN,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }

}