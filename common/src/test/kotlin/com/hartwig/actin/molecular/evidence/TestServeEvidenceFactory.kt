package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.efficacy.Treatment
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

object TestServeEvidenceFactory {

    fun createEvidenceForHotspot(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = ""
    ): EfficacyEvidence {
        return create(TestServeMolecularFactory.createHotspot(gene, chromosome, position, ref, alt))
    }

    fun createEvidenceForCodon(gene: String = ""): EfficacyEvidence {
        return create(TestServeMolecularFactory.createCodon(gene))
    }

    fun createEvidenceForExon(): EfficacyEvidence {
        return create(TestServeMolecularFactory.createExon())
    }

    fun createEvidenceForGene(geneEvent: GeneEvent = GeneEvent.ANY_MUTATION, gene: String = ""): EfficacyEvidence {
        return create(TestServeMolecularFactory.createGene(gene, geneEvent))
    }

    fun createEvidenceForFusion(): EfficacyEvidence {
        return create(TestServeMolecularFactory.createFusion())
    }

    fun createEvidenceForCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): EfficacyEvidence {
        return create(TestServeMolecularFactory.createCharacteristic(type))
    }

    fun createEvidenceForHLA(): EfficacyEvidence {
        return create(TestServeMolecularFactory.createHLA())
    }

    fun create(
        molecularCriterium: MolecularCriterium = TestServeMolecularFactory.createHotspot(),
        source: Knowledgebase = Knowledgebase.CKB,
        treatment: String = "treatment",
        direction: EvidenceDirection = EvidenceDirection.NO_BENEFIT,
        level: EvidenceLevel = EvidenceLevel.D,
        indication: Indication = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("").doid("").build())
            .excludedSubTypes(emptySet()).build()
    ): EfficacyEvidence {
        return object : EfficacyEvidence() {
            override fun source(): Knowledgebase {
                return source
            }

            override fun treatment(): Treatment {
                return ImmutableTreatment.builder().name(treatment).build()
            }

            override fun indication(): Indication {
                return indication
            }

            override fun molecularCriterium(): MolecularCriterium {
                return molecularCriterium
            }

            override fun efficacyDescription(): String {
                return "efficacy evidence"
            }

            override fun evidenceLevel(): EvidenceLevel {
                return level
            }

            override fun evidenceLevelDetails(): EvidenceLevelDetails {
                return EvidenceLevelDetails.GUIDELINE
            }

            override fun evidenceDirection(): EvidenceDirection {
                return direction
            }

            override fun evidenceYear(): Int {
                return 2021
            }

            override fun urls(): Set<String> {
                return emptySet()
            }
        }
    }
}
