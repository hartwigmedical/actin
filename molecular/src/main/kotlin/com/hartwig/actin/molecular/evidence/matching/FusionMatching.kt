package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.serve.datamodel.fusion.FusionPair

object FusionMatching {

    fun isGeneMatch(fusionPair: FusionPair, fusion: FusionMatchCriteria): Boolean {
        return fusionPair.geneUp() == fusion.geneStart && fusionPair.geneDown() == fusion.geneEnd
    }

    fun isExonMatch(fusionPair: FusionPair, fusion: FusionMatchCriteria): Boolean {
        val minExonUp = fusionPair.minExonUp()
        val maxExonUp = fusionPair.maxExonUp()
        val meetsExonUp = minExonUp == null || maxExonUp == null || explicitlyMatchesExonUp(fusionPair, fusion)

        val minExonDown = fusionPair.minExonDown()
        val maxExonDown = fusionPair.maxExonDown()
        val meetsExonDown = minExonDown == null || maxExonDown == null || explicitlyMatchesExonDown(fusionPair, fusion)

        return meetsExonUp && meetsExonDown
    }

    fun explicitlyMatchesExonUp(fusionPair: FusionPair, fusion: FusionMatchCriteria): Boolean {
        val minExonUp = fusionPair.minExonUp()
        val maxExonUp = fusionPair.maxExonUp()
        return if (minExonUp == null || maxExonUp == null) {
            false
        } else {
            fusion.fusedExonUp in minExonUp..maxExonUp
        }
    }

    fun explicitlyMatchesExonDown(fusionPair: FusionPair, fusion: FusionMatchCriteria): Boolean {
        val minExonDown = fusionPair.minExonDown()
        val maxExonDown = fusionPair.maxExonDown()
        return if (minExonDown == null || maxExonDown == null) {
            false
        } else {
            fusion.fusedExonDown in minExonDown..maxExonDown
        }
    }
}
