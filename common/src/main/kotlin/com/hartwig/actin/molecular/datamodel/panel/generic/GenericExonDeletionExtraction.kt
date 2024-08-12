package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

private val EXON_DELETION_REGEX = Regex("ex(\\d+) del")

data class GenericExonDeletionExtraction(
    val gene: String,
    val affectedExon: Int,
) : PanelEvent {
    companion object {
        fun parse(priorIHCTest: PriorIHCTest): GenericExonDeletionExtraction {
            return if (priorIHCTest.item != null && priorIHCTest.measure != null) {
                val exonMatch = EXON_DELETION_REGEX.find(priorIHCTest.measure)
                if (exonMatch != null) {
                    val exon = exonMatch.groupValues[1].toInt()
                    GenericExonDeletionExtraction(gene = priorIHCTest.item, affectedExon = exon)
                } else {
                    throw IllegalArgumentException("Failed to extract exon number for Exon deletion ${priorIHCTest.item} ${priorIHCTest.measure}")
                }
            } else {
                throw IllegalArgumentException("Expected gene and variant but got ${priorIHCTest.item} and ${priorIHCTest.measure}")
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

