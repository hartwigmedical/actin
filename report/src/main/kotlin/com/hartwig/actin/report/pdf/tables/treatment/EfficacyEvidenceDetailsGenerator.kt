package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EfficacyEvidenceDetailsGenerator(
    private val literature: String,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return literature
    }

    override fun contents(): Table {
        val table = Tables.createSingleColWithWidth(width)
        val subtables: MutableList<Table> = mutableListOf()

        subtables.add(createFirstPart())
        subtables.add(createSecondPart())
        subtables.add(createThirdPart())
        subtables.add(createFourthPart())
        subtables.add(createFifthPart())
        subtables.add(createSixthPart())

        for (i in subtables.indices) {
            val subtable = subtables[i]
            table.addCell(Cells.create(subtable))
            if (i < subtables.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        return table
    }

    companion object {
        private fun createFirstPart(): Table {
            val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
            table.addCell(Cells.createValue("Study: "))
            table.addCell(Cells.createKey("TRIBE2, phase III, adjuvant"))
            table.addCell(Cells.createValue("Molecular requirements: "))
            table.addCell(Cells.createKey("None"))
            table.addCell(Cells.createValue("Patient characteristics: "))
            table.addCell(Cells.createKey(""))
            return table
        }

        private fun createSecondPart(): Table {
            val table = Tables.createFixedWidthCols(150f, 100f, 100f).setWidth(350f)
            table.addCell(Cells.createHeader(""))
            table.addCell(Cells.createHeader("FOLFOXORI + bevacizumab (n=239"))
            table.addCell(Cells.createHeader("FOLFIRI + bevacizumab (n=340)"))
            table.addCell(Cells.createContent("Age (median [range])"))
            table.addCell(Cells.createContent("60 [53-67]"))
            table.addCell(Cells.createContent("61 [52-67]"))
            table.addCell(Cells.createContent("Sex"))
            table.addCell(Cells.createContent("Male: 181 \n Female: 158"))
            table.addCell(Cells.createContent("Male: 206 \n Female: 134"))
            table.addCell(Cells.createContent("WHO/ECOG"))
            table.addCell(Cells.createContent("0: 293 \n 1-2: 46"))
            table.addCell(Cells.createContent("0: 289 \n 1-2: 51"))
            return table
        }

        private fun createThirdPart(): Table {
            val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
            table.addCell(Cells.createValue("Primary endpoints: "))
            table.addCell(Cells.createKey(""))
            return table
        }

        private fun createFourthPart(): Table {
            val table = Tables.createFixedWidthCols(100f, 100f, 100f, 100f, 100f).setWidth(500f)
            table.addCell(Cells.createHeader(""))
            table.addCell(Cells.createHeader("FOLFOXORI + bevacizumab"))
            table.addCell(Cells.createHeader("FOLFIRI + bevacizumab"))
            table.addCell(Cells.createHeader("Hazard ratio (HR) / Odds Ratio (OR)"))
            table.addCell(Cells.createHeader("P value"))
            table.addCell(Cells.createContent("Time to second progression (95% CI)"))
            table.addCell(Cells.createContent("19.2 months (17.3-21.4)"))
            table.addCell(Cells.createContent("16.4 months (15.1-17.5)"))
            table.addCell(Cells.createContent("HR 0.74 (0.63-0.88)"))
            table.addCell(Cells.createContent("p = 0.0005"))
            return table
        }

        private fun createFifthPart(): Table {
            val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
            table.addCell(Cells.createValue("Secondary endpoints: "))
            table.addCell(Cells.createKey(""))
            return table
        }

        private fun createSixthPart(): Table {
            val table = Tables.createFixedWidthCols(100f, 100f, 100f, 100f, 100f).setWidth(500f)
            table.addCell(Cells.createHeader(""))
            table.addCell(Cells.createHeader("FOLFOXORI + bevacizumab"))
            table.addCell(Cells.createHeader("FOLFIRI + bevacizumab"))
            table.addCell(Cells.createHeader("Hazard ratio (HR) / Odds Ratio (OR)"))
            table.addCell(Cells.createHeader("P value"))
            table.addCell(Cells.createContent("Median PFS (95% CI)"))
            table.addCell(Cells.createContent("12 months (11.1-12.9)"))
            table.addCell(Cells.createContent("10 months (9.2-11.6)"))
            table.addCell(Cells.createContent("HR 0.75 (0.63-0.88)"))
            table.addCell(Cells.createContent("p = 0.0005"))
            table.addCell(Cells.createSpanningSubNote("Median follow-up was 35.9 months", table))
            return table
        }
    }
}