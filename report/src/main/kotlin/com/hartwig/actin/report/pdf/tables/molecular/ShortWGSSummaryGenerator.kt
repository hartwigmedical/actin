package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.util.ApplicationConfig
import com.itextpdf.layout.element.Table

class ShortWGSSummaryGenerator(
    private val patientRecord: PatientRecord, private val molecular: MolecularTest,
    cohorts: List<EvaluatedCohort>, private val keyWidth: Float, private val valueWidth: Float
) : TableGenerator {
    private val summarizer: MolecularDriversSummarizer =
        MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers, cohorts)
    private val wgsMolecular = molecular as? MolecularRecord
    private val wgsSummaryFunctions = WGSSummaryGeneratorFunctions(patientRecord, molecular, wgsMolecular)

    override fun title(): String {
        return String.format(
            ApplicationConfig.LOCALE,
            "%s of %s (%s)",
            molecular.testTypeDisplay,
            patientRecord.patientId,
            date(molecular.date)
        )
    }

    override fun contents(): Table {
        val characteristicsGenerator = MolecularCharacteristicsGenerator(molecular, keyWidth + valueWidth)
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        if (wgsMolecular?.hasSufficientQuality == true) {
            table.addCell(Cells.createKey("Tumor mutational load / burden"))
            table.addCell(wgsSummaryFunctions.tumorMutationalLoadAndTumorMutationalBurdenStatusCell())
            listOf(
                "Genes with high driver mutation" to formatList(summarizer.keyVariants()),
                "Amplified genes" to formatList(summarizer.keyAmplifiedGenes()),
                "Deleted genes" to formatList(summarizer.keyDeletedGenes()),
                "Homozygously disrupted genes" to formatList(summarizer.keyHomozygouslyDisruptedGenes()),
                "Gene fusions" to formatList(summarizer.keyFusionEvents()),
                "Microsatellite (in)stability" to (characteristicsGenerator.createMSStabilityString() ?: Formats.VALUE_UNKNOWN),
                "" to "",
                "Potentially actionable events with medium/low driver:" to formatList(summarizer.actionableEventsThatAreNotKeyDrivers())
            )
                .filter { (_, value) -> value.isNotEmpty() }
                .flatMap { (key, value) -> listOf(Cells.createKey(key), Cells.createValue(value)) }
                .forEach(table::addCell)
        } else {
            table.addCell(
                Cells.createSpanningContent(
                    "The received biomaterial(s) did not meet the requirements that are needed for "
                            + "high quality whole genome sequencing", table
                )
            )
        }
        return table
    }

    private fun formatList(list: List<String>): String {
        return list.joinToString(Formats.COMMA_SEPARATOR)
    }
}