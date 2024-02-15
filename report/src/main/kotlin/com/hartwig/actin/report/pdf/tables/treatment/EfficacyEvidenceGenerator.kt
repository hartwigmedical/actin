package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EfficacyEvidenceGenerator(
    private val treatments: List<Treatment>,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(100f, width - 250f, 150f).setWidth(width)
        table.addHeaderCell(Cells.createHeader("Treatment"))
        table.addHeaderCell(Cells.createHeader("Literature efficacy evidence"))
        table.addHeaderCell(Cells.createHeader("Database efficacy evidence"))
        treatments.forEach { treatment: Treatment ->
            table.addCell(Cells.createContentBold(treatment.name))
            val literatures = listOf("TRIBE2", "FIRE-3")
            val subtable = Tables.createSingleColWithWidth(width / 2)
            for (literature in literatures) {
                subtable.addCell(Cells.create(createOneLiteraturePart(width, literature)))
            }
            table.addCell(Cells.createContent(subtable))

            table.addCell(Cells.createContent("Not evaluated yet"))
        }
        return table
    }

    companion object {
        private fun createOneLiteraturePart(width: Float, title: String): Table {
            val subtable = Tables.createSingleColWithWidth(width / 2)
            val subsubtables: MutableList<Table> = mutableListOf()
            subsubtables.add(createFirstPart(title))
            subsubtables.add(createSecondPart(width))
            subsubtables.add(createThirdPart())
            for (i in subsubtables.indices) {
                val subsubtable = subsubtables[i]
                subtable.addCell(Cells.create(subsubtable))
                if (i < subsubtables.size - 1) {
                    subtable.addCell(Cells.createEmpty())
                }
            }
            return subtable
        }

        private fun createFirstPart(title: String): Table {
            val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
            table.addCell(Cells.createSubTitle("$title (339 patients)"))
            table.addCell(Cells.createValue(""))
            table.addCell(Cells.createValue("Patient characteristics: "))
            table.addCell(Cells.createKey(""))
            return table
        }

        private fun createSecondPart(width: Float): Table {
            val table = Tables.createFixedWidthCols(40f, 40f).setWidth(width / 2)
            table.addCell(Cells.createContent("WHO/ECOG"))
            table.addCell(Cells.createContent("0: 293, 1-2: 46"))
            table.addCell(Cells.createContent("Primary tumor location"))
            table.addCell(Cells.createContent("Right-sided: 130, Left-sided: 209"))
            table.addCell(Cells.createContent("Mutations"))
            table.addCell(Cells.createContent("RAS/BRAF wt: 74 (22%), RAS mut: 215 (63%), BRAF mut: 33 (10%), Unknown: 17 (5%)"))
            table.addCell(Cells.createContent("Metastatic sites"))
            table.addCell(Cells.createContent("No information"))
            table.addCell(Cells.createContent("Previous adjuvant chemotherapy"))
            table.addCell(Cells.createContent("7 patients"))
            table.addCell(
                Cells.createSpanningSubNote(
                    "This patient matches all patient characteristics of the treatment group, except for age (68 years)",
                    table
                )
            )
            return table
        }

        private fun createThirdPart(): Table {
            val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
            table.addCell(Cells.createValue("Median PFS: "))
            table.addCell(Cells.createKey("12 months (95% CI: 11.1-12.9)"))
            table.addCell(Cells.createValue("Median OS: "))
            table.addCell(Cells.createKey("27.4 months (95% CI: 23.7-30.0)"))
            table.addCell(Cells.createEmpty())
            table.addCell(Cells.createEmpty())
            return table
        }
    }
}