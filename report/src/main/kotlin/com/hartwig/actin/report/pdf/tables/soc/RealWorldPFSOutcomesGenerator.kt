package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.personalization.datamodel.MIN_PATIENT_COUNT
import com.hartwig.actin.personalization.datamodel.MeasurementType
import com.hartwig.actin.personalization.datamodel.SubPopulationAnalysis
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class RealWorldPFSOutcomesGenerator(
    private val analysis: List<SubPopulationAnalysis>,
    private val eligibleTreatments: Set<String>,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return "Median progression-free survival (days) in NCR real-world data set"
    }

    override fun contents(): Table {
        return if (eligibleTreatments.isEmpty()) {
            Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val content = SOCPersonalizedTableContent.fromSubPopulationAnalyses(
                analysis, eligibleTreatments, MeasurementType.PROGRESSION_FREE_SURVIVAL
            ) {
                when {
                    it.value.isNaN() -> TableElement.regular("-")

                    it.numPatients <= MIN_PATIENT_COUNT -> TableElement.regular("n≤$MIN_PATIENT_COUNT")

                    else -> with(it) {
                        val iqrString = if (iqr != null && iqr != Double.NaN) {
                            "IQR: $iqr, "
                        } else ""
                        TableElement(value.toString(), "\n(${iqrString}n=$numPatients)")
                    }
                }
            }
            content.check()
            content.render(width)
        }
    }
}