package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.range.KnownCodon

object HotspotFunctions {

    private val SERVE_HOTSPOT_PROTEIN_EFFECTS = setOf(
        ProteinEffect.LOSS_OF_FUNCTION,
        ProteinEffect.LOSS_OF_FUNCTION_PREDICTED,
        ProteinEffect.GAIN_OF_FUNCTION,
        ProteinEffect.GAIN_OF_FUNCTION_PREDICTED
    )

    fun isHotspot(geneAlteration: GeneAlteration?): Boolean {
        return (geneAlteration is KnownHotspot || geneAlteration is KnownCodon) &&
                geneAlteration.proteinEffect() in SERVE_HOTSPOT_PROTEIN_EFFECTS
    }
}