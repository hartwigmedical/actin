package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration as ServeGeneAlteration
import com.hartwig.serve.datamodel.molecular.common.GeneRole as ServeGeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect

object GeneAlterationFactory {

    fun convertAlteration(
        gene: String,
        input: ServeGeneAlteration?
    ): GeneAlteration {
        return object : GeneAlteration {
            override val gene: String = gene

            override val geneRole: GeneRole = if (input != null) convertGeneRole(input.geneRole()) else GeneRole.UNKNOWN

            override val proteinEffect: ProteinEffect =
                if (input != null) convertProteinEffect(input.proteinEffect()) else ProteinEffect.UNKNOWN

            override val isAssociatedWithDrugResistance: Boolean? = input?.associatedWithDrugResistance()
        }
    }

    private fun convertGeneRole(input: ServeGeneRole): GeneRole {
        return when (input) {
            ServeGeneRole.BOTH -> {
                GeneRole.BOTH
            }

            ServeGeneRole.ONCO -> {
                GeneRole.ONCO
            }

            ServeGeneRole.TSG -> {
                GeneRole.TSG
            }

            ServeGeneRole.UNKNOWN -> {
                GeneRole.UNKNOWN
            }

            else -> {
                throw IllegalStateException("Could not convert gene role input: $input")
            }
        }
    }

    fun convertProteinEffect(input: ServeProteinEffect): ProteinEffect {
        return when (input) {
            ServeProteinEffect.UNKNOWN -> {
                ProteinEffect.UNKNOWN
            }

            ServeProteinEffect.AMBIGUOUS -> {
                ProteinEffect.AMBIGUOUS
            }

            ServeProteinEffect.NO_EFFECT -> {
                ProteinEffect.NO_EFFECT
            }

            ServeProteinEffect.NO_EFFECT_PREDICTED -> {
                ProteinEffect.NO_EFFECT_PREDICTED
            }

            ServeProteinEffect.LOSS_OF_FUNCTION -> {
                ProteinEffect.LOSS_OF_FUNCTION
            }

            ServeProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> {
                ProteinEffect.LOSS_OF_FUNCTION_PREDICTED
            }

            ServeProteinEffect.GAIN_OF_FUNCTION -> {
                ProteinEffect.GAIN_OF_FUNCTION
            }

            ServeProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> {
                ProteinEffect.GAIN_OF_FUNCTION_PREDICTED
            }

            else -> {
                throw IllegalStateException("Could not convert protein effect: $input")
            }
        }
    }
}
