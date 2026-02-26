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

        val table = Tables.createRelativeWidthCols(2f, 3f, 2f, 2f, 5f)

        table.addHeaderCell(Cells.createHeaderWithBorder("HLA gene"))
        table.addHeaderCell(Cells.createHeaderWithBorder("Type"))
        table.addHeaderCell(Cells.createHeaderWithBorder("Tumor copy number"))
        table.addHeaderCell(Cells.createHeaderWithBorder("Mutated in tumor"))

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
                        table.addCell(Cells.createContentNoBorder("HLA-A"))
                    } else {
                        table.addCell(Cells.createContentNoBorder(""))  // Add this 5th cell
                        table.addCell(Cells.createContentNoBorder(""))
                    }


                    val alleleString = "${hlaAllele.gene}*${hlaAllele.alleleGroup}:${hlaAllele.hlaProtein}"
                    table.addCell(Cells.createContentNoBorder(alleleString))

                    val cnDisplay = hlaAllele.tumorCopyNumber?.let { cn ->
                        val boundedCopyNumber = cn.coerceAtLeast(0.0)
                        if (boundedCopyNumber < 1) Formats.forcedSingleDigitNumber(boundedCopyNumber) else Formats.noDigitNumber(
                            boundedCopyNumber
                        )
                    } ?: "-"
                    table.addCell(Cells.createContentNoBorder(cnDisplay))

                    val mutationDisplay = when (hlaAllele.hasSomaticMutations) {
                        true -> "Yes"
                        false -> "No"
                        null -> "-"
                    }
                    table.addCell(Cells.createContentNoBorder(mutationDisplay))
                }
            } else {
                table.addCell(Cells.createKey("HLA-A"))
                table.addCell(Cells.createContentNoBorder("No HLA-A alleles detected"))
                table.addCell(Cells.createContentNoBorder(""))
                table.addCell(Cells.createContentNoBorder(""))
            }
        } ?: run {
            table.addCell(Cells.createKey("HLA-A"))
            table.addCell(Cells.createContentNoBorder("HLA typing not available"))
            table.addCell(Cells.createContentNoBorder(""))
            table.addCell(Cells.createContentNoBorder(""))
        }
    }
}