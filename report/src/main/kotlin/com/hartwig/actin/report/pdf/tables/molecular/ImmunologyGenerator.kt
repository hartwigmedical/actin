package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class ImmunologyGenerator(
    private val molecular: MolecularTest,
    private val displayMode: ImmunologyDisplayMode = ImmunologyDisplayMode.DETAILED_TABLE,
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
            ImmunologyDisplayMode.DETAILED_TABLE -> createDetailedTable()
            ImmunologyDisplayMode.DETAILED_INLINE -> createDetailedInlineTable()
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

    private fun createDetailedInlineTable(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        addHlaAllelesDetailedInline(table)
        return table
    }

    fun addContentsTo(table: Table) {
        addAlleleOnlyContent(table)
    }

    private fun createAlleleOnlyTable(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        addAlleleOnlyContent(table)
        return table
    }

    private fun addAlleleOnlyContent(table: Table) {
        molecular.immunology?.let { immunology ->
            val alleles = relevantAlleles(immunology)
            table.addCell(Cells.createKey("HLA-A"))
            if (alleles.isNotEmpty()) {
                table.addCell(Cells.createValue(alleles.joinToString(", ", transform = ::alleleCompactString)))
            } else {
                table.addCell(Cells.createValue("No HLA-A alleles detected"))
            }
        } ?: run {
            table.addCell(Cells.createKey("HLA-A"))
            table.addCell(Cells.createValue("HLA typing not available"))
        }
    }

    private fun addHlaAAlleles(table: Table) {
        molecular.immunology?.let { immunology ->
            val alleles = relevantAlleles(immunology)

            if (alleles.isNotEmpty()) {
                alleles.forEachIndexed { index, hlaAllele ->
                    if (index == 0) {
                        table.addCell(Cells.createContentNoBorder("HLA-A"))
                    } else {
                        table.addCell(Cells.createContentNoBorder(""))
                        table.addCell(Cells.createContentNoBorder(""))
                    }

                    table.addCell(Cells.createContentNoBorder(alleleCompactString(hlaAllele)))

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

    private fun addHlaAllelesDetailedInline(table: Table) {
        molecular.immunology?.let { immunology ->
            val alleles = relevantAlleles(immunology)

            if (alleles.isNotEmpty()) {
                alleles.forEachIndexed { index, hlaAllele ->
                    table.addCell(Cells.createKey(if (index == 0) "HLA-A" else ""))
                    table.addCell(Cells.createValue(alleleDetailedString(hlaAllele)))
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

    companion object {
        fun relevantAlleles(immunology: MolecularImmunology): List<HlaAllele> =
            immunology.hlaAlleles
                .filter { it.gene == "HLA-A" }
                .sortedBy { "${it.alleleGroup}:${it.hlaProtein}" }

        fun alleleCompactString(allele: HlaAllele): String =
            "${allele.gene}*${allele.alleleGroup}:${allele.hlaProtein}"

        fun alleleDetailedString(allele: HlaAllele): String {
            val cnDisplay = allele.tumorCopyNumber?.let { cn ->
                ", tumor copy nr: ${Formats.noDigitNumber(cn.coerceAtLeast(0.0))}"
            } ?: ""
            val mutationDisplay = when (allele.hasSomaticMutations) {
                true -> ", mutated: Yes"
                false -> ", mutated: No"
                null -> ""
            }
            return "${alleleCompactString(allele)}$cnDisplay$mutationDisplay"
        }
    }
}

enum class ImmunologyDisplayMode {
    DETAILED_TABLE,
    DETAILED_INLINE,
    ALLELE_ONLY
}
