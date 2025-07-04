package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EligibleApprovedTreatmentGenerator(report: Report) : TableGenerator {

    private val isCUP = TumorDetailsInterpreter.hasCancerOfUnknownPrimary(report.patientRecord.tumor.name)
    private val molecular = report.patientRecord.molecularHistory.latestOrangeMolecularRecord()
    private val hasConfidentPrediction =
        molecular?.let { TumorOriginInterpreter.create(molecular).hasConfidentPrediction() } ?: false

    fun showTable(): Boolean {
        return isCUP && hasConfidentPrediction
    }

    override fun title(): String {
        return "Approved treatments considered eligible"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createSingleCol()
        table.addHeaderCell(Cells.createHeader("Treatment"))

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