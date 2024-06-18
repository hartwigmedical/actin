package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.serve.datamodel.common.GeneAlteration

object GeneAlterationFactory {

    fun convertAlteration(
        gene: String,
        input: GeneAlteration?
    ): com.hartwig.actin.molecular.datamodel.GeneAlteration {
        return object : com.hartwig.actin.molecular.datamodel.GeneAlteration {
            override val gene: String = gene

            override val geneRole: GeneRole = if (input != null) convertGeneRole(input.geneRole()) else GeneRole.UNKNOWN

            override val proteinEffect: ProteinEffect =
                if (input != null) convertProteinEffect(input.proteinEffect()) else ProteinEffect.UNKNOWN

            override val isAssociatedWithDrugResistance: Boolean? = input?.associatedWithDrugResistance()
        }
    }

    private fun convertGeneRole(input: com.hartwig.serve.datamodel.common.GeneRole): GeneRole {
        return when (input) {
            com.hartwig.serve.datamodel.common.GeneRole.BOTH -> {
                GeneRole.BOTH
            }

            com.hartwig.serve.datamodel.common.GeneRole.ONCO -> {
                GeneRole.ONCO
            }

            com.hartwig.serve.datamodel.common.GeneRole.TSG -> {
                GeneRole.TSG
            }

            com.hartwig.serve.datamodel.common.GeneRole.UNKNOWN -> {
                GeneRole.UNKNOWN
            }

            else -> {
                throw IllegalStateException("Could not convert gene role input: $input")
            }
        }
    }

    fun convertProteinEffect(input: com.hartwig.serve.datamodel.common.ProteinEffect): ProteinEffect {
        return when (input) {
            com.hartwig.serve.datamodel.common.ProteinEffect.UNKNOWN -> {
                ProteinEffect.UNKNOWN
            }

            com.hartwig.serve.datamodel.common.ProteinEffect.AMBIGUOUS -> {
                ProteinEffect.AMBIGUOUS
            }

            com.hartwig.serve.datamodel.common.ProteinEffect.NO_EFFECT -> {
                ProteinEffect.NO_EFFECT
            }

            com.hartwig.serve.datamodel.common.ProteinEffect.NO_EFFECT_PREDICTED -> {
                ProteinEffect.NO_EFFECT_PREDICTED
            }

            com.hartwig.serve.datamodel.common.ProteinEffect.LOSS_OF_FUNCTION -> {
                ProteinEffect.LOSS_OF_FUNCTION
            }

            com.hartwig.serve.datamodel.common.ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> {
                ProteinEffect.LOSS_OF_FUNCTION_PREDICTED
            }

            com.hartwig.serve.datamodel.common.ProteinEffect.GAIN_OF_FUNCTION -> {
                ProteinEffect.GAIN_OF_FUNCTION
            }

            com.hartwig.serve.datamodel.common.ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> {
                ProteinEffect.GAIN_OF_FUNCTION_PREDICTED
            }

            else -> {
                throw IllegalStateException("Could not convert protein effect: $input")
            }
        }
    }
}
