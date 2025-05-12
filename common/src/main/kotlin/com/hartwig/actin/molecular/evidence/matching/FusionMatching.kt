package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.serve.datamodel.molecular.fusion.FusionPair

object FusionMatching {

    fun isGeneMatch(fusionPair: FusionPair, fusion: Fusion): Boolean {
        return fusionPair.geneUp() == fusion.geneStart && fusionPair.geneDown() == fusion.geneEnd
    }

    fun isExonMatch(fusionPair: FusionPair, fusion: Fusion): Boolean {
        val minExonUp = fusionPair.minExonUp()
        val maxExonUp = fusionPair.maxExonUp()
        val meetsExonUp = minExonUp == null || maxExonUp == null || explicitlyMatchesExonUp(fusionPair, fusion)

        val minExonDown = fusionPair.minExonDown()
        val maxExonDown = fusionPair.maxExonDown()
        val meetsExonDown = minExonDown == null || maxExonDown == null || explicitlyMatchesExonDown(fusionPair, fusion)

        return meetsExonUp && meetsExonDown
    }

    fun explicitlyMatchesExonUp(fusionPair: FusionPair, fusion: Fusion): Boolean {
        val minExonUp = fusionPair.minExonUp()
        val maxExonUp = fusionPair.maxExonUp()
        return if (minExonUp == null || maxExonUp == null) {
            false
        } else {
            fusion.fusedExonUp in minExonUp..maxExonUp
        }
    }

    fun explicitlyMatchesExonDown(fusionPair: FusionPair, fusion: Fusion): Boolean {
        val minExonDown = fusionPair.minExonDown()
        val maxExonDown = fusionPair.maxExonDown()
        return if (minExonDown == null || maxExonDown == null) {
            false
        } else {
            fusion.fusedExonDown in minExonDown..maxExonDown
        }
    }
}
