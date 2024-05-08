package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

private val EXON_DELETION_REGEX = Regex("ex(\\d+) del")

data class GenericExonDeletion(
    val gene: String,
    val affectedExon: Int,
) : PanelEvent {
    companion object {
        fun parse(priorMolecularTest: PriorMolecularTest): GenericExonDeletion {
            return if (priorMolecularTest.item != null && priorMolecularTest.measure != null) {
                val exonMatch = EXON_DELETION_REGEX.find(priorMolecularTest.measure)
                if (exonMatch != null) {
                    val exon = exonMatch.groupValues[1].toInt()
                    GenericExonDeletion(gene = priorMolecularTest.item, affectedExon = exon)
                } else {
                    throw IllegalArgumentException("Failed to extract exon number for Exon deletion ${priorMolecularTest.item} ${priorMolecularTest.measure}")
                }
            } else {
                throw IllegalArgumentException("Expected gene and variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}")
            }
        }
    }

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return "$gene exon $affectedExon deletion"
    }
}

