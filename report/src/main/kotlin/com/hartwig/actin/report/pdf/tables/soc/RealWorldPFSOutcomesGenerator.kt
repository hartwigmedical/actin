package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.personalization.MIN_PATIENT_COUNT
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.datamodel.personalization.PersonalizedDataAnalysis
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class RealWorldPFSOutcomesGenerator(
    private val analysis: PersonalizedDataAnalysis,
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
            val content = SOCPersonalizedTableContent.fromPersonalizedDataAnalysis(
                analysis, eligibleTreatments, MeasurementType.PROGRESSION_FREE_SURVIVAL
            ) {
                when {
                    it.value.isNaN() -> TableElement.regular("-")

                    it.numPatients <= MIN_PATIENT_COUNT -> TableElement.regular(NA)

                    else -> with(it) {
                        val iqrString = iqr?.takeUnless(Double::isNaN)?.let { ", IQR: " + Formats.singleDigitNumber(it) } ?: ""
                        TableElement(Formats.singleDigitNumber(value), "$iqrString\n(n=$numPatients)")
                    }
                }
            }
            content.check()
            content.render(width)
        }
    }
}