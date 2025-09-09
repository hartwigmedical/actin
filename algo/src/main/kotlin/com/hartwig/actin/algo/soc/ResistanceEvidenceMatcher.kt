package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.evidence.actionability.MatchesForActionable
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.efficacy.Treatment as ServeTreatment

class ResistanceEvidenceMatcher(
    private val candidateEvidences: List<EfficacyEvidence>,
    private val treatmentDatabase: TreatmentDatabase,
    private val molecularTestsAndMatches: List<Pair<MolecularTest, MatchesForActionable>>
) {

    fun match(treatment: Treatment): List<ResistanceEvidence> {
        return candidateEvidences.mapNotNull { evidence ->
            findTreatmentInDatabase(evidence.treatment(), treatment)?.let { treatmentName ->
                ResistanceEvidence(
                    event = findSourceEvent(evidence),
                    treatmentName = treatmentName,
                    resistanceLevel = evidence.evidenceLevel().toString(),
                    isTested = null,
                    isFound = isFound(evidence),
                    evidenceUrls = evidence.urls()
                )
            }
        }
    }

    fun isFound(evidence: EfficacyEvidence): Boolean? {

        with(evidence.molecularCriterium()) {
            return when {
                hotspots().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.variants.any { hasEvidence(it, evidence, molecularTest.second) }
                    }
                }

                codons().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.variants.any { hasEvidence(it, evidence, molecularTest.second) }
                    }
                }

                exons().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.variants.any { hasEvidence(it, evidence, molecularTest.second) }
                    }
                }

                genes().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        with(molecularTest.first.drivers) {
                            val variantMatch = variants.any { hasEvidence(it, evidence, molecularTest.second) }
                            val fusionMatch = fusions.any { hasEvidence(it, evidence, molecularTest.second) }
                            variantMatch || fusionMatch ||
                                    copyNumbers.any { hasEvidence(it, evidence, molecularTest.second) } ||
                                    homozygousDisruptions.any { hasEvidence(it, evidence, molecularTest.second) } ||
                                    disruptions.any { hasEvidence(it, evidence, molecularTest.second) }
                        }
                    }
                }

                fusions().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.fusions.any {
                            hasEvidence(it, evidence, molecularTest.second)
                        }
                    }
                }

                // TODO (CB): Also look for resistance for HLA and characteristics?
                else -> null
            }
        }
    }

    private fun hasEvidence(it: Actionable, evidence: EfficacyEvidence, matches: MatchesForActionable) =
        matches[it]?.evidenceMatches?.contains(evidence) == true

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
            molecularTests: List<MolecularTest>,
            actionabilityMatcher: ActionabilityMatcher
        ): ResistanceEvidenceMatcher {
            val expandedTumorDoids = expandDoids(doidModel, tumorDoids)
            val onLabelNonPositiveEvidence = evidences.filter { hasNoPositiveResponse(it) && isOnLabel(it, expandedTumorDoids) }
            val molecularTestsAndMatches = molecularTests.map { it to actionabilityMatcher.match(it) }

            return ResistanceEvidenceMatcher(
                onLabelNonPositiveEvidence,
                treatmentDatabase,
                molecularTestsAndMatches
            )
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