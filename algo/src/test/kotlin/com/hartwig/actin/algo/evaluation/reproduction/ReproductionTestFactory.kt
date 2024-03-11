package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.Gender

internal object ReproductionTestFactory {
    private val base = TestDataFactory.createMinimalTestPatientRecord()
    
    fun withGender(gender: Gender): PatientRecord {
        return base.copy(patient = base.patient.copy(gender = gender))
    }
}