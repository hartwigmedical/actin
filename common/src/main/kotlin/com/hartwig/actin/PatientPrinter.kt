package com.hartwig.actin

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.molecular.util.MolecularTestPrinter
import com.hartwig.actin.util.DatamodelPrinter

class PatientPrinter(private val printer: DatamodelPrinter) {

    fun print(record: PatientRecord) {
        printer.print("Patient: " + record.patientId)
        printer.print("Birth year: " + record.patient.birthYear)
        printer.print("Gender: " + record.patient.gender)
        printer.print("Primary tumor: " + record.tumor.name)
        printer.print("WHO status: " + record.performanceStatus.latestWho)

        MolecularTestPrinter(printer).print(record.molecularTests)
    }

    companion object {
        fun printRecord(record: PatientRecord) {
            PatientPrinter(DatamodelPrinter.withDefaultIndentation()).print(record)
        }
    }
}