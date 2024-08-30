package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Gender

internal object ReproductionTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    
    fun withGender(gender: Gender): PatientRecord {
        return base.copy(patient = base.patient.copy(gender = gender))
    }
}