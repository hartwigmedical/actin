package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class EligibleApprovedTreatmentGenerator(private val patientRecord: PatientRecord, private val width: Float) : TableGenerator {

    override fun title(): String {
        return "Approved treatments considered eligible"
    }

    override fun contents(): Table {
        val table = Tables.createSingleColWithWidth(width)
        table.addHeaderCell(Cells.createHeader("Treatment"))
        val isCUP = TumorDetailsInterpreter.isCUP(patientRecord.tumor)
        val molecular = patientRecord.molecularHistory.latestOrangeMolecularRecord()
        val hasConfidentPrediction =
            molecular?.let { TumorOriginInterpreter(molecular.characteristics.predictedTumorOrigin).hasConfidentPrediction() } ?: false
        if (isCUP && hasConfidentPrediction) {
            table.addCell(Cells.createContent("Potential SOC for " + molecular!!.characteristics.predictedTumorOrigin!!.cancerType()))
        } else {
            table.addCell(Cells.createContent("Not yet determined"))
        }
        return makeWrapping(table)
    }
}