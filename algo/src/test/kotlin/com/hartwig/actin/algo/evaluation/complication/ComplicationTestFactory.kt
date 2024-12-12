package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Medication

internal object ComplicationTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    
    fun complication(name: String = "", categories: Set<String> = emptySet(), icdCode: IcdCode = IcdCode("")): Complication {
        return Complication(name = name, categories = categories, icdCode = icdCode, year = null, month = null)
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
            tumor = base.tumor.copy(
                hasCnsLesions = true, otherLesions = listOf(lesion)
            )
        )
    }

    fun withSuspectedCnsLesion(lesion: String): PatientRecord {
        return base.copy(
            tumor = base.tumor.copy(
                hasCnsLesions = false, hasSuspectedCnsLesions = true, otherLesions = listOf(lesion)
            )
        )
    }
}