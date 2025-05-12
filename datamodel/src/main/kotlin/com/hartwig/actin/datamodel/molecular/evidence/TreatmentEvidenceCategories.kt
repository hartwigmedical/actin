package com.hartwig.actin.datamodel.molecular.evidence

object TreatmentEvidenceCategories {

    fun approved(treatmentEvidence: Set<TreatmentEvidence>) =
        responsive(treatmentEvidence).filter { it.evidenceLevel == EvidenceLevel.A && it.evidenceDirection.isCertain }

    fun experimental(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(
            responsive(treatmentEvidence),
            onLabel
        ).filter {
            (it.evidenceLevel == EvidenceLevel.A && !it.evidenceDirection.isCertain) ||
                    (it.evidenceLevel == EvidenceLevel.B && it.evidenceDirection.isCertain)

        }

    fun preclinical(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(
            responsive(treatmentEvidence),
            onLabel
        ).filter {
            (it.evidenceLevel == EvidenceLevel.B && !it.evidenceDirection.isCertain) ||
                    it.evidenceLevel == EvidenceLevel.C || it.evidenceLevel == EvidenceLevel.D
        }

    fun knownResistant(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(
            resistant(treatmentEvidence),
            onLabel
        ).filter { (it.evidenceLevel == EvidenceLevel.A || it.evidenceLevel == EvidenceLevel.B) && it.evidenceDirection.isCertain }

    fun suspectResistant(treatmentEvidence: Set<TreatmentEvidence>, onLabel: Boolean? = null) =
        filterOnLabel(resistant(treatmentEvidence), onLabel)
            .filter {
                ((it.evidenceLevel == EvidenceLevel.A || it.evidenceLevel == EvidenceLevel.B) && !it.evidenceDirection.isCertain) ||
                        it.evidenceLevel == EvidenceLevel.C || it.evidenceLevel == EvidenceLevel.D
            }

    private fun responsive(treatmentEvidence: Set<TreatmentEvidence>) =
        treatmentEvidence.filter { it.evidenceDirection.hasPositiveResponse }

    private fun resistant(treatmentEvidence: Set<TreatmentEvidence>) = treatmentEvidence.filter { it.evidenceDirection.isResistant }

    private fun filterOnLabel(
        treatmentEvidence: Collection<TreatmentEvidence>,
        onLabel: Boolean?
    ) = treatmentEvidence.filter { onLabel?.let { l -> l == it.isOnLabel() } ?: true }
}