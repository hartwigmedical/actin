package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
import com.hartwig.serve.datamodel.molecular.common.GeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.gene.KnownGene

object GeneLookup {

    fun find(knownGenes: Set<KnownGene>, gene: String): GeneAlteration? {
        return GeneAggregator.aggregate(knownGenes).find { it.gene() == gene }?.let { fromKnownGene(it) }
    }

    private fun fromKnownGene(knownGene: KnownGene): GeneAlteration {
        return object : GeneAlteration {
            override fun geneRole(): GeneRole {
                return knownGene.geneRole()
            }

            override fun proteinEffect(): ProteinEffect {
                return ProteinEffect.UNKNOWN
            }

            override fun associatedWithDrugResistance(): Boolean? {
                return null
            }
        }
    }
}
