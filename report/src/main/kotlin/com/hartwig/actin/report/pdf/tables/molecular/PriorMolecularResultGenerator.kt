package com.hartwig.actin.report.pdf.tables.molecular

import com.google.common.collect.Lists
import com.google.common.collect.Sets
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
import java.util.*

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
        private fun generatePriorTestParagraphs(interpretation: PriorMolecularTestInterpretation): List<Paragraph?> {
            val paragraphs: MutableList<Paragraph?> = Lists.newArrayList()
            val sortedKeys: MutableSet<PriorMolecularTestKey> = Sets.newTreeSet(PriorMolecularTestKeyComparator())
            sortedKeys.addAll(interpretation.textBasedPriorTests().keySet())
            for (key in sortedKeys) {
                paragraphs.add(Paragraph(formatTextBasedPriorTests(key, interpretation.textBasedPriorTests()[key])))
            }
            val sortedValueTests: List<PriorMolecularTest?> = Lists.newArrayList(interpretation.valueBasedPriorTests())
            sortedValueTests.sort(PriorMolecularTestComparator())
            for (valueTest in sortedValueTests) {
                paragraphs.add(Paragraph(formatValueBasedPriorTest(valueTest)))
            }
            return paragraphs
        }

        private fun formatTextBasedPriorTests(key: PriorMolecularTestKey, values: Collection<PriorMolecularTest?>): String {
            val sorted: List<PriorMolecularTest?> = Lists.newArrayList(values)
            sorted.sort(PriorMolecularTestComparator())
            val builder = StringBuilder()
            val scoreText = key.scoreText()
            builder.append(scoreText.substring(0, 1).uppercase(Locale.getDefault()))
            if (scoreText.length > 1) {
                builder.append(scoreText.substring(1).lowercase(Locale.getDefault()))
            }
            builder.append(" (")
            builder.append(key.test())
            builder.append("): ")
            builder.append(sorted[0]!!.item())
            for (i in 1 until sorted.size) {
                if (i < sorted.size - 1) {
                    builder.append(", ")
                } else {
                    builder.append(" and ")
                }
                builder.append(sorted[i]!!.item())
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