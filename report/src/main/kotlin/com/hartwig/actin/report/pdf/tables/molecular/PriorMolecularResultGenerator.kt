package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.sort.PriorMolecularTestComparator
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpretation
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpreter
import com.hartwig.actin.report.interpretation.PriorMolecularTestKey
import com.hartwig.actin.report.interpretation.PriorMolecularTestKeyComparator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class PriorMolecularResultGenerator(private val clinical: ClinicalRecord, private val keyWidth: Float, private val valueWidth: Float) {
    fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createSubTitle("IHC results"))
        if (clinical.priorMolecularTests().isEmpty()) {
            table.addCell(Cells.createValue("None"))
        } else {
            val interpretation = PriorMolecularTestInterpreter.interpret(clinical.priorMolecularTests())
            val paragraphs = generatePriorTestParagraphs(interpretation)
            table.addCell(Cells.createValue(paragraphs))
        }
        return table
    }

    companion object {
        private fun generatePriorTestParagraphs(interpretation: PriorMolecularTestInterpretation): List<Paragraph> {
            val sortedTextBasedPriorTests = interpretation.textBasedPriorTests.keySet()
                .sortedWith(PriorMolecularTestKeyComparator())
                .map { formatTextBasedPriorTests(it, interpretation.textBasedPriorTests[it]) }

            val sortedValueBasedTests = interpretation.valueBasedPriorTests
                .sortedWith(PriorMolecularTestComparator())
                .map(::formatValueBasedPriorTest)

            return listOf(sortedTextBasedPriorTests, sortedValueBasedTests).flatten().map { Paragraph(it) }
        }

        private fun formatTextBasedPriorTests(key: PriorMolecularTestKey, values: Collection<PriorMolecularTest>): String {
            val sorted = values.sortedWith(PriorMolecularTestComparator())
            val builder = StringBuilder()
            val scoreText = key.scoreText()
            builder.append(scoreText.substring(0, 1).uppercase())
            if (scoreText.length > 1) {
                builder.append(scoreText.substring(1).lowercase())
            }
            builder.append(" (")
            builder.append(key.test())
            builder.append("): ")
            builder.append(sorted[0].item())
            for (i in 1 until sorted.size) {
                if (i < sorted.size - 1) {
                    builder.append(", ")
                } else {
                    builder.append(" and ")
                }
                builder.append(sorted[i].item())
            }
            return builder.toString()
        }

        private fun formatValueBasedPriorTest(valueTest: PriorMolecularTest): String {
            val builder = StringBuilder()
            builder.append("Score ")
            builder.append(valueTest.item())
            builder.append(" ")
            val measure = valueTest.measure()
            if (measure != null) {
                builder.append(measure)
                builder.append(" ")
            }
            val scoreValuePrefix = valueTest.scoreValuePrefix()
            if (scoreValuePrefix != null) {
                builder.append(scoreValuePrefix)
                builder.append(" ")
            }
            builder.append(Formats.twoDigitNumber(valueTest.scoreValue()!!))
            val scoreValueUnit = valueTest.scoreValueUnit()
            if (scoreValueUnit != null) {
                builder.append(" ")
                builder.append(scoreValueUnit)
            }
            return builder.toString()
        }
    }
}