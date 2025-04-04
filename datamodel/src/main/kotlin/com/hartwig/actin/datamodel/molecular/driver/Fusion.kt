package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class Fusion(
    val geneStart: String,
    val geneEnd: String,
    val geneContextStart: String = "",
    val geneContextEnd: String = "",
    val driverType: FusionDriverType,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean?,
    val geneTranscriptStart: String?,
    val geneTranscriptEnd: String?,
    val fusedExonUp: Int?,
    val fusedExonDown: Int?,
    val fusionPhasedType: FusionPhasedType = FusionPhasedType.INFRAME,
    val junctionCopyNumber: Double = 0.0,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
) : Driver, Comparable<Fusion> {

    override fun compareTo(other: Fusion): Int {
        return Comparator.comparing<Fusion, Fusion>({ it }, DriverComparator())
            .thenComparing(Fusion::geneStart)
            .thenComparing(Fusion::geneEnd)
            .thenComparing { fusion -> fusion.geneTranscriptStart.orEmpty() }
            .thenComparing { fusion -> fusion.geneTranscriptEnd.orEmpty() }
            .compare(this, other)
    }
}