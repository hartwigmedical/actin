package com.hartwig.actin

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.molecular.util.MolecularHistoryPrinter
import com.hartwig.actin.util.DatamodelPrinter

class PatientPrinter(private val printer: DatamodelPrinter) {

    fun print(record: PatientRecord) {
        printer.print("Patient: " + record.patientId)
        printer.print("Birth year: " + record.patient.birthYear)
        printer.print("Gender: " + record.patient.gender)
        printer.print("Primary tumor location: " + record.tumor.name)
        printer.print("Primary tumor type: " + tumorType(record.tumor))
        printer.print("WHO status: " + record.clinicalStatus.who)

        MolecularHistoryPrinter.print(record.molecularHistory)
    }

    companion object {
        fun printRecord(record: PatientRecord) {
            PatientPrinter(DatamodelPrinter.withDefaultIndentation()).print(record)
        }

        private fun tumorType(tumor: TumorDetails): String? {
            val type = tumor.primaryTumorType ?: return null
            val subType = tumor.primaryTumorSubType
            return if (!subType.isNullOrEmpty()) subType else type
        }
    }
}