package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Medication

internal object ComplicationTestFactory {
    private val base = TestPatientFactory.createMinimalTestPatientRecord()
    
    fun complication(name: String = "", categories: Set<String> = emptySet()): Complication {
        return Complication(name = name, categories = categories, year = null, month = null)
    }

    fun yesInputComplication(): Complication {
        return complication()
    }

    fun withComplication(complication: Complication): PatientRecord {
        return withComplications(listOf(complication))
    }

    fun withComplications(complications: List<Complication>?): PatientRecord {
        return base.copy(
            complications = complications,
            clinicalStatus = ClinicalStatus(hasComplications = complications?.isNotEmpty())
        )
    }

    fun withMedication(medication: Medication): PatientRecord {
        return base.copy(medications = listOf(medication))
    }

    fun withCnsLesion(lesion: String): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(hasCnsLesions = true, otherLesions = listOf(lesion)
            )
        )
    }
}