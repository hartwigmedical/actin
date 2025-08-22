package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.panel.PanelRecord
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcherFactory
import com.hartwig.actin.molecular.evidence.actionability.CancerTypeApplicabilityResolver
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.serve.datamodel.ServeRecord

object EvidenceAnnotatorFactory {

    fun createPanelRecordAnnotator(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>,
        patientGender: Gender
    ): EvidenceAnnotator<PanelRecord> {
        return create(serveRecord, doidEntry, tumorDoids, patientGender) { input, drivers, molecularCharacteristics ->
            input.copy(drivers = drivers, characteristics = molecularCharacteristics)
        }
    }

    fun createMolecularRecordAnnotator(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): EvidenceAnnotator<MolecularRecord> {
        return create(serveRecord, doidEntry, tumorDoids, null) { input, drivers, molecularCharacteristics ->
            input.copy(drivers = drivers, characteristics = molecularCharacteristics)
        }
    }

    private fun <T : MolecularTest> create(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>,
        patientGender: Gender?,
        annotationFunction: (T, Drivers, MolecularCharacteristics) -> T
    ): EvidenceAnnotator<T> {
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val cancerTypeResolver = CancerTypeApplicabilityResolver.create(doidModel, tumorDoids)
        val clinicalEvidenceFactory = ClinicalEvidenceFactory(cancerTypeResolver, patientGender)
        val actionabilityMatcher = ActionabilityMatcherFactory.create(serveRecord)

        return EvidenceAnnotator(
            clinicalEvidenceFactory = clinicalEvidenceFactory,
            actionabilityMatcher = actionabilityMatcher,
            annotationFunction = annotationFunction
        )
    }
}