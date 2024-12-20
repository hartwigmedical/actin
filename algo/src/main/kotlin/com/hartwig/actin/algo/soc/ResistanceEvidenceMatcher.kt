package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceMatcher
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceMatcherFactory
import com.hartwig.actin.molecular.evidence.actionability.CopyNumberEvidence
import com.hartwig.actin.molecular.evidence.actionability.DisruptionEvidence
import com.hartwig.actin.molecular.evidence.actionability.FusionEvidence
import com.hartwig.actin.molecular.evidence.actionability.HomozygousDisruptionEvidence
import com.hartwig.actin.molecular.evidence.actionability.VariantEvidence
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.efficacy.Treatment as ServeTreatment

class ResistanceEvidenceMatcher(
    private val candidateEvidences: List<EfficacyEvidence>,
    private val treatmentDatabase: TreatmentDatabase,
    // TODO (CB): Use clinicalEvidenceMatcher to generate all matches and then simplify this function?
    @Suppress("unused") private val clinicalEvidenceMatcher: ClinicalEvidenceMatcher,
    private val molecularHistory: MolecularHistory
) {

    fun match(treatment: Treatment): List<ResistanceEvidence> {
        return candidateEvidences.mapNotNull { evidence ->
            findTreatmentInDatabase(evidence.treatment(), treatment)?.let { treatmentName ->
                ResistanceEvidence(
                    event = findSourceEvent(evidence),
                    treatmentName = treatmentName,
                    resistanceLevel = evidence.evidenceLevel().toString(),
                    isTested = null,
                    isFound = isFound(evidence, molecularHistory),
                    evidenceUrls = evidence.urls()
                )
            }
        }.distinctBy { it.event }
    }

    fun isFound(evidence: EfficacyEvidence, molecularHistory: MolecularHistory): Boolean? {
        val molecularTests = molecularHistory.molecularTests

        val variantEvidence = VariantEvidence.create(evidences = listOf(evidence), trials = emptyList())
        val copyNumberEvidence = CopyNumberEvidence.create(evidences = listOf(evidence), trials = emptyList())
        val disruptionEvidence = DisruptionEvidence.create(evidences = listOf(evidence), trials = emptyList())
        val homDisEvidence = HomozygousDisruptionEvidence.create(evidences = listOf(evidence), trials = emptyList())
        val fusionEvidence = FusionEvidence.create(evidences = listOf(evidence), trials = emptyList())

        with(evidence.molecularCriterium()) {
            return when {
                hotspots().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any {
                            val variantCriteria = MatchingCriteriaFunctions.createVariantCriteria(it)
                            variantEvidence.findMatches(variantCriteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                codons().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any {
                            val variantCriteria = MatchingCriteriaFunctions.createVariantCriteria(it)
                            variantEvidence.findMatches(variantCriteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                exons().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any {
                            val variantCriteria = MatchingCriteriaFunctions.createVariantCriteria(it)
                            variantEvidence.findMatches(variantCriteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                genes().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        with(molecularTest.drivers) {
                            val variantMatch = variants.any {
                                val variantCriteria = MatchingCriteriaFunctions.createVariantCriteria(it)
                                variantEvidence.findMatches(variantCriteria).evidenceMatches.isNotEmpty()
                            }
                            val fusionMatch = fusions.any {
                                val fusionCriteria = MatchingCriteriaFunctions.createFusionCriteria(it)
                                fusionEvidence.findMatches(fusionCriteria).evidenceMatches.isNotEmpty()
                            }
                            variantMatch || fusionMatch ||
                                    copyNumbers.any { copyNumberEvidence.findMatches(it).evidenceMatches.isNotEmpty() } ||
                                    homozygousDisruptions.any { homDisEvidence.findMatches(it).evidenceMatches.isNotEmpty() } ||
                                    disruptions.any { disruptionEvidence.findMatches(it).evidenceMatches.isNotEmpty() }
                        }
                    }
                }

                fusions().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.fusions.any {
                            val fusionCriteria = MatchingCriteriaFunctions.createFusionCriteria(it)
                            fusionEvidence.findMatches(fusionCriteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                // TODO (CB): Also look for resistance for HLA and characteristics?
                else -> null
            }
        }
    }

    private fun findTreatmentInDatabase(treatment: ServeTreatment, treatmentToFind: Treatment): String? {
        return EfficacyEntryFactory(treatmentDatabase).generateOptions(listOf(treatment.name()))
            .mapNotNull(treatmentDatabase::findTreatmentByName)
            .distinct()
            .singleOrNull()
            ?.takeIf { drugsInOtherTreatment(treatmentToFind, it) }
            ?.name
    }

    private fun drugsInOtherTreatment(treatment1: Treatment, treatment2: Treatment): Boolean {
        val drugs1 = (treatment1 as DrugTreatment).drugs
        val drugs2 = (treatment2 as DrugTreatment).drugs
        return drugs1.containsAll(drugs2)
    }

    private fun findSourceEvent(evidence: EfficacyEvidence): String {
        // Assumes there is no combined/complex evidence yet
        with(evidence.molecularCriterium()) {
            return when {
                hotspots().isNotEmpty() -> {
                    hotspots().iterator().next().sourceEvent()
                }

                codons().isNotEmpty() -> {
                    codons().iterator().next().sourceEvent()
                }

                exons().isNotEmpty() -> {
                    exons().iterator().next().sourceEvent()
                }

                genes().isNotEmpty() -> {
                    genes().iterator().next().sourceEvent()
                }

                fusions().isNotEmpty() -> {
                    fusions().iterator().next().sourceEvent()
                }

                characteristics().isNotEmpty() -> {
                    characteristics().iterator().next().sourceEvent()
                }

                hla().isNotEmpty() -> {
                    hla().iterator().next().sourceEvent()
                }

                else -> ""
            }
        }
    }

    companion object {
        fun create(
            doidModel: DoidModel,
            tumorDoids: Set<String>,
            evidences: List<EfficacyEvidence>,
            treatmentDatabase: TreatmentDatabase,
            molecularHistory: MolecularHistory
        ): ResistanceEvidenceMatcher {
            val expandedTumorDoids = expandDoids(doidModel, tumorDoids)
            val onLabelNonPositiveEvidence = evidences.filter { hasNoPositiveResponse(it) && isOnLabel(it, expandedTumorDoids) }

            val clinicalEvidenceMatcherFactory = ClinicalEvidenceMatcherFactory(doidModel, tumorDoids)
            val actionableEventMatcher = clinicalEvidenceMatcherFactory.create(evidences = onLabelNonPositiveEvidence, trials = emptyList())

            return ResistanceEvidenceMatcher(onLabelNonPositiveEvidence, treatmentDatabase, actionableEventMatcher, molecularHistory)
        }

        private fun isOnLabel(event: EfficacyEvidence, expandedTumorDoids: Set<String>): Boolean {
            if (!expandedTumorDoids.contains(event.indication().applicableType().doid())) {
                return false
            }
            return event.indication().excludedSubTypes().none { expandedTumorDoids.contains(it.doid()) }
        }

        private fun expandDoids(doidModel: DoidModel, doids: Set<String>): Set<String> {
            return DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, doids)
        }

        private fun hasNoPositiveResponse(resistanceEvent: EfficacyEvidence): Boolean {
            return resistanceEvent.evidenceLevel() in setOf(
                EvidenceLevel.A,
                EvidenceLevel.B,
                EvidenceLevel.C
            ) && !resistanceEvent.evidenceDirection().hasPositiveResponse()
        }
    }
}