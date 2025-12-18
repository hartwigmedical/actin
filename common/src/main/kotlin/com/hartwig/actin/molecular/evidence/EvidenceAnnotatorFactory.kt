package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
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
        patientGender: Gender?
    ): EvidenceAnnotator {
        return create(serveRecord, doidEntry, tumorDoids, patientGender) { input, drivers, molecularCharacteristics, immunology ->
            input.copy(drivers = drivers, characteristics = molecularCharacteristics, immunology = immunology)
        }
    }

    fun createMolecularRecordAnnotator(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>,
        patientGender: Gender?
    ): EvidenceAnnotator {
        return create(serveRecord, doidEntry, tumorDoids, patientGender) { input, drivers, molecularCharacteristics, immunology ->
            input.copy(drivers = drivers, characteristics = molecularCharacteristics, immunology = immunology)
        }
    }

    private fun create(
        serveRecord: ServeRecord,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>,
        patientGender: Gender?,
        annotationFunction: (MolecularTest, Drivers, MolecularCharacteristics, MolecularImmunology?) -> MolecularTest
    ): EvidenceAnnotator {
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