package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.interpreted.InterpretedFusion

data class PanelFusion(
    override val geneStart: String,
    override val geneEnd: String,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence
) : InterpretedFusion, PanelEvent {
    override fun impactsGene(gene: String): Boolean {
        return this.geneStart == gene || this.geneEnd == gene
    }

    override fun display(): String {
        return "$geneStart $geneEnd fusion"
    }
}