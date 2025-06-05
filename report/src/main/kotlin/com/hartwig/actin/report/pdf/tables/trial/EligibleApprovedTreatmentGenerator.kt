package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EligibleApprovedTreatmentGenerator(private val report: Report) : TableGenerator {

    override fun title(): String {
        return "Approved treatments considered eligible"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createSingleCol()
        table.addHeaderCell(Cells.createHeader("Treatment"))

        val isCUP = TumorDetailsInterpreter.isCUP(report.patientRecord.tumor)
        val molecular = report.patientRecord.molecularHistory.latestOrangeMolecularRecord()
        val hasConfidentPrediction =
            molecular?.let { TumorOriginInterpreter.create(molecular).hasConfidentPrediction() } ?: false

        when {
            isCUP && hasConfidentPrediction -> {
                table.addCell(Cells.createContent("Potential SOC for " + molecular!!.characteristics.predictedTumorOrigin!!.cancerType()))
            }

            else -> {
                table.addCell(Cells.createContent("Not yet determined"))
            }
        }
        return table
    }
}