package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.personalization.datamodel.MeasurementType
import com.hartwig.actin.personalization.datamodel.SubPopulationAnalysis
import com.hartwig.actin.personalization.similarity.report.SOCPersonalizedTableContent
import com.hartwig.actin.personalization.similarity.report.TableElement
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
        return "Progression-free survival (median (range)) in NCR real-world data set"
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

                    it.numPatients <= 5 -> TableElement.regular("n≤5")

                    else -> with(it) { TableElement(value.toString(), " (${min}-${max}) \n(n=${numPatients})") }
                }
            }
            content.check()
            content.render(width)
        }
    }
}