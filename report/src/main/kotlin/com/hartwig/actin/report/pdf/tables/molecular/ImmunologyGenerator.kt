package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class ImmunologyGenerator(private val molecular: MolecularTest) : TableGenerator {

    override fun title(): String {
        return "Immunology"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(25f, 35f, 20f, 20f)

        table.addHeaderCell(Cells.createHeader("Type"))
        table.addHeaderCell(Cells.createHeader("Allele Status"))
        table.addHeaderCell(Cells.createHeader("CN"))
        table.addHeaderCell(Cells.createHeader("Mutation"))

        addHlaAAlleles(table)

        return table
    }

    private fun addHlaAAlleles(table: Table) {
        molecular.immunology?.let { immunology ->
            val hlaAAlleles = immunology.hlaAlleles
                .filter { it.gene == "HLA-A" }
                .sortedBy { "${it.alleleGroup}:${it.hlaProtein}" }

            if (hlaAAlleles.isNotEmpty()) {
                hlaAAlleles.forEachIndexed { index, hlaAllele ->
                    if (index == 0) {
                        table.addCell(Cells.createContent("HLA-A"))
                    } else {
                        table.addCell(Cells.createContentNoBorder(""))
                    }

                    val alleleString = "${hlaAllele.gene}*${hlaAllele.alleleGroup}:${hlaAllele.hlaProtein}"
                    table.addCell(Cells.createContent(alleleString))

                    val cnDisplay = hlaAllele.tumorCopyNumber?.let { cn ->
                        val boundedCopyNumber = cn.coerceAtLeast(0.0)
                        if (boundedCopyNumber < 1) Formats.forcedSingleDigitNumber(boundedCopyNumber) else Formats.noDigitNumber(
                            boundedCopyNumber
                        )
                    } ?: "-"
                    table.addCell(Cells.createContent(cnDisplay))

                    val mutationDisplay = when (hlaAllele.hasSomaticMutations) {
                        true -> "Yes"
                        false -> "No"
                        null -> "-"
                    }
                    table.addCell(Cells.createContent(mutationDisplay))
                }
            } else {
                table.addCell(Cells.createKey("HLA-A"))
                table.addCell(Cells.createContent("No HLA-A alleles detected"))
                table.addCell(Cells.createContent(""))
                table.addCell(Cells.createContent(""))
            }
        } ?: run {
            table.addCell(Cells.createKey("HLA-A"))
            table.addCell(Cells.createContent("HLA typing not available"))
            table.addCell(Cells.createContent(""))
            table.addCell(Cells.createContent(""))
        }
    }
}