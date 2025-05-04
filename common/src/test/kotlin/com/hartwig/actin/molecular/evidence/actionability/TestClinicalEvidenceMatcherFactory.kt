package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

private const val MATCHING_DOID = "matching doid"

class TestClinicalEvidenceMatcherFactory : ClinicalEvidenceMatcherFactory {

    override fun create(molecularTest: MolecularTest): ClinicalEvidenceMatcher {
        val personalizedActionabilityFactory = PersonalizedActionabilityFactory(setOf(MATCHING_DOID))

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

        // TODO the set of associated actionables may not be consistent with unit tests that use this
        val actionableToEvidences: ActionableToEvidences = mapOf(
            create("gene 1", CopyNumberType.FULL_GAIN) to setOf(
                TestServeEvidenceFactory.createEvidenceForGene(
                    gene = "gene 1",
                    geneEvent = GeneEvent.AMPLIFICATION
                )
            ),
            create("gene 1", CopyNumberType.LOSS) to setOf(
                TestServeEvidenceFactory.createEvidenceForGene(
                    gene = "gene 1",
                    geneEvent = GeneEvent.DELETION
                )
            ),
            create("gene 1", CopyNumberType.NONE) to setOf(
                TestServeEvidenceFactory.createEvidenceForGene(
                    gene = "gene 1",
                    geneEvent = GeneEvent.INACTIVATION
                )
            ),
            create("gene 1", CopyNumberType.FULL_GAIN) to setOf(TestServeEvidenceFactory.createEvidenceForHla())
        )

        return ClinicalEvidenceMatcher(
            personalizedActionabilityFactory = personalizedActionabilityFactory,
            variantEvidence = VariantEvidence.create(actionableToEvidences, trials),
            copyNumberEvidence = CopyNumberEvidence.create(actionableToEvidences, trials),
            disruptionEvidence = DisruptionEvidence.create(actionableToEvidences, trials),
            homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableToEvidences, trials),
            fusionEvidence = FusionEvidence.create(actionableToEvidences, trials),
            virusEvidence = VirusEvidence.create(actionableToEvidences, trials),
            signatureEvidence = SignatureEvidence.create(evidences, trials),
        )
    }

    private fun createTrialWithCriterium(molecularCriterium: MolecularCriterium): ActionableTrial {
        return TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(molecularCriterium),
            indications = setOf(TestServeFactory.createIndicationWithDoid(MATCHING_DOID))
        )
    }

    // TODO redundant with CopyNumberEvidenceTest, move to somewhere common if we stick with this
    private fun create(gene: String, copyNumberType: CopyNumberType): CopyNumber {
        return TestMolecularFactory.minimalCopyNumber().copy(
            gene = gene,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(copyNumberType)
        )
    }
}
