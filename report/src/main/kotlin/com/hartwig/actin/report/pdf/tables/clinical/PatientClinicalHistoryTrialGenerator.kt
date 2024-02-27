package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells.create
import com.hartwig.actin.report.pdf.util.Cells.createKey
import com.hartwig.actin.report.pdf.util.Tables.createFixedWidthCols
import com.itextpdf.layout.element.Table

class PatientClinicalHistoryTrialGenerator(
    private val record: ClinicalRecord,
    private val keyWidth: Float,
    override val valueWidth: Float
) :
    TableGenerator, PatientClinicalHistoryGenerator {
    override fun title(): String {
        return "Clinical summary"
    }

    override fun contents(): Table {
        val table = createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(createKey("Relevant systemic treatment history"))
        table.addCell(create(tableOrNone(relevantSystemicPreTreatmentHistoryTable(record))))
        table.addCell(createKey("Relevant other oncological history"))
        table.addCell(create(tableOrNone(relevantNonSystemicPreTreatmentHistoryTable(record))))
        table.addCell(createKey("Previous primary tumor"))
        table.addCell(create(tableOrNone(secondPrimaryHistoryTable(record))))
        table.addCell(createKey("Relevant non-oncological history"))
        table.addCell(create(tableOrNone(relevantNonOncologicalHistoryTable(record))))
        return table
    }
}