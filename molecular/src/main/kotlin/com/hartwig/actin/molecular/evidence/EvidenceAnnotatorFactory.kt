package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.molecular.evidence.actionability.CancerTypeApplicabilityResolver
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.CombinedEvidenceMatcherFactory
import com.hartwig.serve.datamodel.ServeRecord

object EvidenceAnnotatorFactory {
    fun create(serveRecord: ServeRecord, doidEntry: DoidEntry, tumorDoids: Set<String>): EvidenceAnnotator {
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val cancerTypeResolver = CancerTypeApplicabilityResolver.create(doidModel, tumorDoids)
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver)
        val combinedEvidenceMatcher = CombinedEvidenceMatcherFactory.create(serveRecord)

        return EvidenceAnnotator(
            clinicalEvidenceFactory = clinicalEvidenceFactory,
            actionabilityMatcher = combinedEvidenceMatcher,
        )
    }
}