package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.interpreted.InterpretedVariant

class PanelVariant(
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?,
    override val chromosome: String,
    override val position: Int,
    override val ref: String,
    override val alt: String,
    override val type: VariantType,
    override val isHotspot: Boolean,
    override val canonicalImpact: TranscriptImpact
) : InterpretedVariant, PanelEvent {
    override val clonalLikelihood = 0.0
    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return event
    }
}