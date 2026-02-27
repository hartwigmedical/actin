package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table


    override fun title(): String {
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {


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
                    } else {
                        table.addCell(Cells.createContentNoBorder(""))
                    }

                    val alleleString = "${hlaAllele.gene}*${hlaAllele.alleleGroup}:${hlaAllele.hlaProtein}"

                    val cnDisplay = hlaAllele.tumorCopyNumber?.let { cn ->
                        val boundedCopyNumber = cn.coerceAtLeast(0.0)
                        if (boundedCopyNumber < 1) Formats.forcedSingleDigitNumber(boundedCopyNumber) else Formats.noDigitNumber(
                            boundedCopyNumber
                        )
                    } ?: "-"

                    val mutationDisplay = when (hlaAllele.hasSomaticMutations) {
                        true -> "Yes"
                        false -> "No"
                        null -> "-"
                    }
                }
            } else {
                table.addCell(Cells.createKey("HLA-A"))
            }
        } ?: run {
            table.addCell(Cells.createKey("HLA-A"))
        }
    }
}