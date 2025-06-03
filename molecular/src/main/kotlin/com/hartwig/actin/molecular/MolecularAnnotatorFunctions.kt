package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantAlteration

object MolecularAnnotatorFunctions {

    fun annotateProteinEffect(variant: Variant, alteration: VariantAlteration): ProteinEffect {
        return if (variant.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT && alteration.geneRole == GeneRole.TSG) {
            ProteinEffect.LOSS_OF_FUNCTION
        } else alteration.proteinEffect
    }
}