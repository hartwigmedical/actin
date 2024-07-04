package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.personalization.datamodel.MeasurementType
import com.hartwig.actin.personalization.datamodel.PersonalizedDataAnalysis
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class RealWorldTreatmentDecisionsGenerator(
    private val analysis: PersonalizedDataAnalysis,
    private val eligibleTreatments: Set<String>,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return "Treatment decisions (percentage of population assigned to systemic treatment) in NCR real-world data set"
    }

    override fun contents(): Table {
        return if (eligibleTreatments.isEmpty()) {
            Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val content = SOCPersonalizedTableContent.fromPersonalizedDataAnalysis(
                analysis, eligibleTreatments, MeasurementType.TREATMENT_DECISION
            ) { TableElement.regular(Formats.singleDigitPercentage(it.value)) }
            content.check()
            content.render(width)
        }
    }
}