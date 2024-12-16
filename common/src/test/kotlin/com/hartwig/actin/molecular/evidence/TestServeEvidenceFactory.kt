package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
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
        return create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                gene = gene,
                chromosome = chromosome,
                position = position,
                ref = ref,
                alt = alt
            )
        )
    }

    fun createEvidenceForCodon(gene: String = ""): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createCodonCriterium(gene = gene))
    }

    fun createEvidenceForExon(): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createExonCriterium())
    }

    fun createEvidenceForGene(gene: String = "", geneEvent: GeneEvent = GeneEvent.ANY_MUTATION): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = gene, geneEvent = geneEvent))
    }

    fun createEvidenceForFusion(): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createFusionCriterium())
    }

    fun createEvidenceForCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createCharacteristicCriterium(type = type))
    }

    fun createEvidenceForHLA(): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createHLACriterium())
    }

    fun create(
        source: Knowledgebase = ActionabilityConstants.EVIDENCE_SOURCE,
        treatment: String = "treatment",
        indication: Indication = TestServeFactory.createEmptyIndication(),
        molecularCriterium: MolecularCriterium = TestServeMolecularFactory.createHotspotCriterium(),
        evidenceLevel: EvidenceLevel = EvidenceLevel.D,
        evidenceLevelDetails: EvidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
        evidenceDirection: EvidenceDirection = EvidenceDirection.NO_BENEFIT
    ): EfficacyEvidence {
        return ImmutableEfficacyEvidence.builder()
            .source(source)
            .treatment(ImmutableTreatment.builder().name(treatment).build())
            .indication(indication)
            .molecularCriterium(molecularCriterium)
            .efficacyDescription("efficacy description")
            .evidenceLevel(evidenceLevel)
            .evidenceLevelDetails(evidenceLevelDetails)
            .evidenceDirection(evidenceDirection)
            .evidenceYear(2021)
            .urls(emptySet())
            .build()
    }
}
