package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium

object CombinedEvidenceMatcherFactory {

    fun create(serveRecord: ServeRecord): CombinedEvidenceMatcher {
        return CombinedEvidenceMatcher(filterEvidences(serveRecord.evidences()))
    }

    private fun filterEvidences(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return evidences
            .filter { it.source() == ActionabilityConstants.EVIDENCE_SOURCE }
            .filter { isMolecularCriteriumApplicable(it.molecularCriterium()) }
    }

    private fun isMolecularCriteriumApplicable(molecularCriterium: MolecularCriterium): Boolean {
        with(molecularCriterium) {
            return hotspots().all { ApplicabilityFiltering.isApplicable(it) } &&
                    codons().all { ApplicabilityFiltering.isApplicable(it) } &&
                    exons().all { ApplicabilityFiltering.isApplicable(it) } &&
                    genes().all { ApplicabilityFiltering.isApplicable(it) }
        }
    }
}