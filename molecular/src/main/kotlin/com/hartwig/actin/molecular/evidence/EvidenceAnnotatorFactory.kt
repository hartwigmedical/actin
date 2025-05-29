package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.molecular.evidence.actionability.CancerTypeApplicabilityResolver
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.CombinedEvidenceMatcherFactory
import com.hartwig.serve.datamodel.ServeRecord

object EvidenceAnnotatorFactory {
    fun <T : MolecularTest> create(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>,
        annotationFunction: (T, Drivers, MolecularCharacteristics) -> T
    ): EvidenceAnnotator<T> {
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val cancerTypeResolver = CancerTypeApplicabilityResolver.create(doidModel, tumorDoids)
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver)
        val combinedEvidenceMatcher = CombinedEvidenceMatcherFactory.create(serveRecord)

        return EvidenceAnnotator(
            clinicalEvidenceFactory = clinicalEvidenceFactory,
            actionabilityMatcher = combinedEvidenceMatcher,
            annotationFunction
        )
    }

    fun createPanelRecordAnnotator(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): EvidenceAnnotator<PanelRecord> {
        return create(serveRecord, doidEntry, tumorDoids) { input, drivers, molecularCharacteristics ->
            input.copy(drivers = drivers, characteristics = molecularCharacteristics)
        }
    }

    fun createMolecularRecordAnnotator(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): EvidenceAnnotator<MolecularRecord> {
        return create(serveRecord, doidEntry, tumorDoids) { input, drivers, molecularCharacteristics ->
            input.copy(drivers = drivers, characteristics = molecularCharacteristics)
        }
    }
}