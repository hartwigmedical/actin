package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.clinical.datamodel.Gender

internal object ReproductionTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    
    fun withGender(gender: Gender): PatientRecord {
        return base.copy(patient = base.patient.copy(gender = gender))
    }
}