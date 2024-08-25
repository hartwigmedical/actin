package com.hartwig.actin.molecular.datamodel.evidence

object ClinicalEvidenceCategories {

    fun approved(treatmentEvidence: Set<TreatmentEvidence>) =
        responsive(treatmentEvidence).filter { it.evidenceLevel == EvidenceLevel.A && it.direction.isCertain }

    fun experimental(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(
            responsive(treatmentEvidence),
            onLabel
        ).filter {
            (it.evidenceLevel == EvidenceLevel.A && !it.direction.isCertain) ||
                    (it.evidenceLevel == EvidenceLevel.B && it.direction.isCertain)

        }

    fun preclinical(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(
            responsive(treatmentEvidence),
            onLabel
        ).filter {
            (it.evidenceLevel == EvidenceLevel.B && !it.direction.isCertain) ||
                    it.evidenceLevel == EvidenceLevel.C || it.evidenceLevel == EvidenceLevel.D
        }

    fun knownResistant(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(
            resistant(treatmentEvidence),
            onLabel
        ).filter { (it.evidenceLevel == EvidenceLevel.A || it.evidenceLevel == EvidenceLevel.B) && it.direction.isCertain }

    fun suspectResistant(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(resistant(treatmentEvidence), onLabel)
            .filter {
                ((it.evidenceLevel == EvidenceLevel.A || it.evidenceLevel == EvidenceLevel.B) && !it.direction.isCertain) ||
                        it.evidenceLevel == EvidenceLevel.C || it.evidenceLevel == EvidenceLevel.D
            }

    private fun responsive(treatmentEvidence: Set<TreatmentEvidence>) = treatmentEvidence.filter { it.direction.hasPositiveResponse }

    private fun resistant(treatmentEvidence: Set<TreatmentEvidence>) = treatmentEvidence.filter { it.direction.isResistant }

    private fun filterOnLabel(
        treatmentEvidence: Collection<TreatmentEvidence>,
        onLabel: Boolean?
    ) = treatmentEvidence.filter { onLabel?.let { l -> l == it.onLabel } ?: true }
}