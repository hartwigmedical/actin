package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

data class PanelVariant(
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
) : Variant, PanelEvent {
    override val clonalLikelihood = 0.5
    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return event
    }
}