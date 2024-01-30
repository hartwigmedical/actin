package com.hartwig.actin.clinical.util

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.util.DatamodelPrinter
import com.hartwig.actin.util.DatamodelPrinter.Companion.withDefaultIndentation

class ClinicalPrinter private constructor(private val printer: DatamodelPrinter) {

    fun print(record: ClinicalRecord) {
        printer.print("Patient: " + record.patientId)
        printer.print("Birth year: " + record.patient.birthYear)
        printer.print("Gender: " + record.patient.gender)
        printer.print("Primary tumor location: " + tumorLocation(record.tumor))
        printer.print("Primary tumor type: " + tumorType(record.tumor))
        printer.print("WHO status: " + record.clinicalStatus.who)
    }

    companion object {
        fun printRecord(record: ClinicalRecord) {
            ClinicalPrinter(withDefaultIndentation()).print(record)
        }

        private fun tumorLocation(tumor: TumorDetails): String? {
            val location = tumor.primaryTumorLocation ?: return null
            val subLocation = tumor.primaryTumorSubLocation
            return if (!subLocation.isNullOrEmpty()) "$location ($subLocation)" else location
        }

        private fun tumorType(tumor: TumorDetails): String? {
            val type = tumor.primaryTumorType ?: return null
            val subType = tumor.primaryTumorSubType
            return if (!subType.isNullOrEmpty()) subType else type
        }
    }
}
