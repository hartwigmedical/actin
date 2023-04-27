package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.Medication
import org.apache.logging.log4j.util.Strings

internal object ComplicationTestFactory {
    fun builder(): ImmutableComplication.Builder {
        return ImmutableComplication.builder().name(Strings.EMPTY)
    }

    fun yesInputComplication(): Complication {
        return builder().build()
    }

    fun withComplication(complication: Complication): PatientRecord {
        return withComplications(listOf(complication))
    }

    fun withComplications(complications: List<Complication?>?): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder().from(base).clinical(
            ImmutableClinicalRecord.builder().from(base.clinical()).complications(complications).clinicalStatus(
                ImmutableClinicalStatus.builder().hasComplications(complications?.isNotEmpty()).build()
            ).build()
        ).build()
    }

    fun withMedication(medication: Medication): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder().from(base)
            .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).medications(listOf(medication)).build()).build()
    }

    fun withCnsLesion(lesion: String): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder().from(base).clinical(
            ImmutableClinicalRecord.builder().from(base.clinical()).tumor(
                ImmutableTumorDetails.builder().from(base.clinical().tumor()).hasCnsLesions(true).addOtherLesions(lesion).build()
            ).build()
        ).build()
    }
}