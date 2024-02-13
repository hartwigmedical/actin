package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class EligibleApprovedTreatmentGenerator(
    private val clinical: ClinicalRecord, private val molecular: MolecularRecord, private val treatments: List<Treatment>?,
    private val width: Float, private val mode: String
) : TableGenerator {

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun contents(): Table {
        when (mode) {
            "Other" -> {
                val table = Tables.createSingleColWithWidth(width)
                table.addHeaderCell(Cells.createHeader("Treatment"))
                val isCUP = TumorDetailsInterpreter.isCUP(clinical.tumor)
                val hasConfidentPrediction = TumorOriginInterpreter.hasConfidentPrediction(molecular.characteristics.predictedTumorOrigin)
                if (isCUP && hasConfidentPrediction) {
                    table.addCell(Cells.createContent("Potential SOC for " + molecular.characteristics.predictedTumorOrigin!!.cancerType()))
                } else {
                    table.addCell(Cells.createContent("Not yet determined"))
                }
                return makeWrapping(table)
            }

            "CRC" -> {
                val table = Tables.createFixedWidthCols(1f, 1f, 1f).setWidth(width)
                table.addHeaderCell(Cells.createHeader("Treatment"))
                table.addHeaderCell(Cells.createHeader("Literature based"))
                table.addHeaderCell(Cells.createHeader("Personalized PFS"))
                treatments?.forEach { treatment: Treatment ->
                    table.addCell(Cells.createContentBold(treatment.name))
                    table.addCell(Cells.createContent("PFS: 12 months (95% CI: 11.1-12.9) \n OS: 27.4 months (95% CI: 23.7-30.0) \n (TRIBE2)"))
                    table.addCell(Cells.createContent("Not evaluated yet"))
                }
                return makeWrapping(table)
            }

            else -> throw IllegalStateException("Unknown mode: $mode")
        }
    }
}