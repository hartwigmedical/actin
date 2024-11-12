package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.personalization.MIN_PATIENT_COUNT
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.datamodel.personalization.PersonalizedDataAnalysis
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class RealWorldSurvivalOutcomesGenerator(
    private val analysis: PersonalizedDataAnalysis,
    private val eligibleTreatments: Set<String>,
    private val width: Float,
    private val measurementType: MeasurementType
) : TableGenerator {

    override fun title(): String {
        return when (measurementType) {
            MeasurementType.PROGRESSION_FREE_SURVIVAL -> "Median progression-free survival (months) in NCR real-world data set"
            MeasurementType.OVERALL_SURVIVAL -> "Median overall survival (months) in NCR real-world data set"
            MeasurementType.TREATMENT_DECISION -> error("Unexpected measurement type: TREATMENT_DECISION")
        }
    }

    override fun contents(): Table {
        val filteredTreatments = if (measurementType == MeasurementType.PROGRESSION_FREE_SURVIVAL) {
            eligibleTreatments.filterNot { it.equals("None", ignoreCase = true) }
        } else {
            eligibleTreatments
        }

        return if (filteredTreatments.isEmpty()) {
            Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val content = SOCPersonalizedTableContent.fromPersonalizedDataAnalysis(
                analysis, filteredTreatments.toSet(), measurementType
            ) {
                when {
                    it.value.isNaN() -> TableElement.regular("-")

                    it.numPatients <= MIN_PATIENT_COUNT -> TableElement.regular(NA)

                    else -> with(it) {
                        val iqrString = iqr?.takeUnless(Double::isNaN)?.let { ", IQR: " + Formats.daysToMonths(it) } ?: ""
                        TableElement(Formats.daysToMonths(value), "$iqrString\n(n=$numPatients)")
                    }
                }
            }
            content.check()
            content.render(width)
        }
    }
}
