package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class ImmunologyGenerator(
    private val molecular: MolecularTest,
    private val displayMode: ImmunologyDisplayMode = ImmunologyDisplayMode.DETAILED,
    private val title: String = "Immunology",
    private val keyWidth: Float,
    private val valueWidth: Float,
) : TableGenerator {

    override fun title(): String {
        return title
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        return when (displayMode) {
            ImmunologyDisplayMode.DETAILED -> createDetailedTable()
            ImmunologyDisplayMode.SUMMARY -> createSummaryTable()
            ImmunologyDisplayMode.ALLELE_ONLY -> createAlleleOnlyTable()
        }
    }

    private fun createDetailedTable(): Table {
        val table = Tables.createRelativeWidthCols(2f, 3f, 2f, 2f, 5f)

        table.addHeaderCell(Cells.createHeaderWithBorder("HLA gene"))
        table.addHeaderCell(Cells.createHeaderWithBorder("Type"))
        table.addHeaderCell(Cells.createHeaderWithBorder("Tumor copy number"))
        table.addHeaderCell(Cells.createHeaderWithBorder("Mutated in tumor"))

        addHlaAAlleles(table)

        return table
    }

    private fun createSummaryTable(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        addHlaAllelesForSummary(table)
        return table
    }

    private fun createAlleleOnlyTable(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        molecular.immunology?.let { immunology ->
            val hlaAAlleles = immunology.hlaAlleles
                .filter { it.gene == "HLA-A" }
                .sortedBy { "${it.alleleGroup}:${it.hlaProtein}" }

            table.addCell(Cells.createKey("HLA-A"))
            if (hlaAAlleles.isNotEmpty()) {
                val alleleNames = hlaAAlleles.joinToString(", ") { "${it.gene}*${it.alleleGroup}:${it.hlaProtein}" }
                table.addCell(Cells.createValue(alleleNames))
            } else {
                table.addCell(Cells.createValue("No HLA-A alleles detected"))
            }
        } ?: run {
            table.addCell(Cells.createKey("HLA-A"))
            table.addCell(Cells.createValue("HLA typing not available"))
        }
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
                        table.addCell(Cells.createContentNoBorder(""))
                        table.addCell(Cells.createContentNoBorder(""))
                    }

                    val alleleString = "${hlaAllele.gene}*${hlaAllele.alleleGroup}:${hlaAllele.hlaProtein}"
                    table.addCell(Cells.createContentNoBorder(alleleString))

                    val cnDisplay = hlaAllele.tumorCopyNumber?.let { cn ->
                        Formats.forcedSingleDigitNumber(cn.coerceAtLeast(0.0))
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

    private fun addHlaAllelesForSummary(table: Table) {
        molecular.immunology?.let { immunology ->
            val hlaAAlleles = immunology.hlaAlleles
                .filter { it.gene == "HLA-A" }
                .sortedBy { "${it.alleleGroup}:${it.hlaProtein}" }

            if (hlaAAlleles.isNotEmpty()) {
                hlaAAlleles.forEachIndexed { index, hlaAllele ->

                    val geneCell = if (index == 0) "HLA-A" else ""
                    table.addCell(Cells.createKey(geneCell))

                    val alleleString = "${hlaAllele.gene}*${hlaAllele.alleleGroup}:${hlaAllele.hlaProtein}"
                    val cnDisplay = hlaAllele.tumorCopyNumber?.let { cn ->
                        ", tumor copy nr: ${Formats.noDigitNumber(cn.coerceAtLeast(0.0))}"
                    } ?: ""

                    val mutationDisplay = when (hlaAllele.hasSomaticMutations) {
                        true -> ", mutated: Yes"
                        false -> ", mutated: No"
                        null -> ""
                    }

                    val fullText = "$alleleString$cnDisplay$mutationDisplay"
                    table.addCell(Cells.createValue(fullText))
                }
            } else {
                table.addCell(Cells.createKey("HLA-A"))
                table.addCell(Cells.createValue("No HLA-A alleles detected"))
            }
        } ?: run {
            table.addCell(Cells.createKey("HLA-A"))
            table.addCell(Cells.createValue("HLA typing not available"))
        }
    }
}

enum class ImmunologyDisplayMode {
    DETAILED,
    SUMMARY,
    ALLELE_ONLY
}