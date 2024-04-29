package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelVariant

data class GenericVariant(
    val gene: String,
    val hgvsCodingImpact: String
) : PanelVariant {
    companion object {
        fun parseVariant(priorMolecularTest: PriorMolecularTest): GenericVariant {
            return if (priorMolecularTest.item != null && priorMolecularTest.measure != null) {
                GenericVariant(gene = priorMolecularTest.item, hgvsCodingImpact = priorMolecularTest.measure)
            } else {
                throw IllegalArgumentException("Expected item and measure for variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}")
            }
        }
    }

    override fun event(): String {
        return "$gene $hgvsCodingImpact"
    }
}