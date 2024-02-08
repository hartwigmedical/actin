package com.hartwig.actin.algo.ckb.json

data class JsonCkbEfficacyEvidence(
    val id: Int,
    val approvalStatus: String,
    val evidenceType: String,
    val efficacyEvidence: String,
    val molecularProfile: JsonCkbMolecularProfile,
    val therapy: JsonCkbTherapy,
    val indication: JsonCkbIndication,
    val responseType: String,
    val references: List<JsonCkbReference>,
    val ampCapAscoEvidenceLevel: String,
    val ampCapAscoInferredTier: String,
    val referencedMetrics: List<JsonCkbEndPointMetric>
)
