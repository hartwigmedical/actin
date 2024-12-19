package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.Country
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial

object TestServeTrialFactory {

    fun createTrialForHotspot(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = ""
    ): ActionableTrial {
        return create(
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createHotspotCriterium(
                    gene = gene,
                    chromosome = chromosome,
                    position = position,
                    ref = ref,
                    alt = alt
                )
            )
        )
    }

    fun createTrialForCodon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): ActionableTrial {
        return create(
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createCodonCriterium(
                    gene = gene,
                    chromosome = chromosome,
                    start = start,
                    end = end,
                    applicableMutationType = applicableMutationType
                )
            )
        )
    }

    fun createTrialForExon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): ActionableTrial {
        return create(
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createExonCriterium(
                    gene = gene,
                    chromosome = chromosome,
                    start = start,
                    end = end,
                    applicableMutationType = applicableMutationType
                )
            )
        )
    }

    fun createTrialForGene(gene: String = "", geneEvent: GeneEvent = GeneEvent.ANY_MUTATION): ActionableTrial {
        return create(
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createGeneCriterium(
                    gene = gene,
                    geneEvent = geneEvent
                )
            )
        )
    }

    fun createTrialForFusion(): ActionableTrial {
        return create(anyMolecularCriteria = setOf(TestServeMolecularFactory.createFusionCriterium()))
    }


    fun createTrialForCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): ActionableTrial {
        return create(anyMolecularCriteria = setOf(TestServeMolecularFactory.createCharacteristicCriterium(type = type)))
    }

    fun createTrialForHla(): ActionableTrial {
        return create(anyMolecularCriteria = setOf(TestServeMolecularFactory.createHlaCriterium()))
    }

    fun create(
        source: Knowledgebase = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
        nctId: String = "NCT00000001",
        title: String = "",
        acronym: String = "",
        countries: Set<Country> = emptySet(),
        indications: Set<Indication> = emptySet(),
        anyMolecularCriteria: Set<MolecularCriterium> = emptySet(),
        urls: Set<String> = setOf("https://clinicaltrials.gov/study/$nctId")
    ): ActionableTrial {
        return ImmutableActionableTrial.builder()
            .source(source)
            .nctId(nctId)
            .title(title)
            .acronym(acronym)
            .countries(countries)
            .therapyNames(emptySet())
            .genderCriterium(null)
            .indications(indications)
            .anyMolecularCriteria(anyMolecularCriteria)
            .urls(urls)
            .build()
    }
}