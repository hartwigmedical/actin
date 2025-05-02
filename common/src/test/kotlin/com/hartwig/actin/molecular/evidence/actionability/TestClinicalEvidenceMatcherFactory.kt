package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

private const val MATCHING_DOID = "matching doid"

object TestClinicalEvidenceMatcherFactory {

    fun createProper(): ClinicalEvidenceMatcher {

        val evidences = listOf(
            TestServeEvidenceFactory.createEvidenceForHotspot(),
            TestServeEvidenceFactory.createEvidenceForCodon(),
            TestServeEvidenceFactory.createEvidenceForExon(),
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.DELETION),
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.AMPLIFICATION),
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.ANY_MUTATION),
            TestServeEvidenceFactory.createEvidenceForFusion(),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HPV_POSITIVE),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.EBV_POSITIVE),
            TestServeEvidenceFactory.createEvidenceForHla()
        )

        val trials = listOf(
            createTrialWithCriterium(TestServeMolecularFactory.createHotspotCriterium()),
            createTrialWithCriterium(TestServeMolecularFactory.createCodonCriterium()),
            createTrialWithCriterium(TestServeMolecularFactory.createExonCriterium()),
            createTrialWithCriterium(TestServeMolecularFactory.createGeneCriterium(geneEvent = GeneEvent.AMPLIFICATION)),
            createTrialWithCriterium(TestServeMolecularFactory.createGeneCriterium(geneEvent = GeneEvent.DELETION)),
            createTrialWithCriterium(TestServeMolecularFactory.createGeneCriterium(geneEvent = GeneEvent.ANY_MUTATION)),
            createTrialWithCriterium(TestServeMolecularFactory.createFusionCriterium()),
            createTrialWithCriterium(TestServeMolecularFactory.createCharacteristicCriterium(type = TumorCharacteristicType.HPV_POSITIVE)),
            createTrialWithCriterium(TestServeMolecularFactory.createCharacteristicCriterium(type = TumorCharacteristicType.EBV_POSITIVE)),
            createTrialWithCriterium(
                TestServeMolecularFactory.createCharacteristicCriterium(
                    type = TumorCharacteristicType.MICROSATELLITE_UNSTABLE
                )
            ),
            createTrialWithCriterium(
                TestServeMolecularFactory.createCharacteristicCriterium(
                    type = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN
                )
            ),
            createTrialWithCriterium(
                TestServeMolecularFactory.createCharacteristicCriterium(
                    type = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD
                )
            ),
            createTrialWithCriterium(
                TestServeMolecularFactory.createCharacteristicCriterium(
                    type = TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT
                )
            ),
            createTrialWithCriterium(TestServeMolecularFactory.createHlaCriterium()),
        )

        return ClinicalEvidenceMatcher(
            clinicalEvidenceFactory = ClinicalEvidenceFactory(
                EvidenceCancerTypeResolver.create(
                    TestDoidModelFactory.createMinimalTestDoidModel(),
                    setOf(MATCHING_DOID)
                )
            ),
            variantEvidence = VariantEvidence.create(evidences, trials),
            copyNumberEvidence = CopyNumberEvidence.create(evidences, trials),
            disruptionEvidence = DisruptionEvidence.create(evidences, trials),
            homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(evidences, trials),
            fusionEvidence = FusionEvidence.create(evidences, trials),
            virusEvidence = VirusEvidence.create(evidences, trials),
            signatureEvidence = SignatureEvidence.create(evidences, trials)
        )
    }

    private fun createTrialWithCriterium(molecularCriterium: MolecularCriterium): ActionableTrial {
        return TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(molecularCriterium),
            indications = setOf(TestServeFactory.createIndicationWithDoid(MATCHING_DOID))
        )
    }
}
