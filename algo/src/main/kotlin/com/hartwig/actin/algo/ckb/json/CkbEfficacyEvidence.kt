package com.hartwig.actin.algo.ckb.json

data class CkbEfficacyEvidence(
    val id: Int,
    val approvalStatus: String,
    val evidenceType: String,
    val efficacyEvidence: String,
    val molecularProfile: CkbMolecularProfile,
    val therapy: CkbTherapy,
    val indication: CkbIndication,
    val responseType: String,
    val references: List<CkbReference>,
    val ampCapAscoEvidenceLevel: String,
    val ampCapAscoInferredTier: String,
    val referencedMetrics: List<CkbEndPointMetric>
)