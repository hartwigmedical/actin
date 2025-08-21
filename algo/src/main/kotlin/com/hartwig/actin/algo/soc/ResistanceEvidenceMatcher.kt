package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.evidence.actionability.MatchesForActionable
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.hartwig.serve.datamodel.efficacy.Treatment as ServeTreatment

class ResistanceEvidenceMatcher(
    private val candidateEvidences: List<EfficacyEvidence>,
    private val treatmentDatabase: TreatmentDatabase,
    private val actionabilityMatcher: ActionabilityMatcher,
    private val molecularHistory: MolecularHistory
) {

    val LOGGER: Logger = LogManager.getLogger(ResistanceEvidenceMatcher::class.java)

    fun match(treatment: Treatment): List<ResistanceEvidence> {
        val molecularTestsAndMatches = molecularHistory.molecularTests.map { it to actionabilityMatcher.match(it) }
        LOGGER.warn("molecularTestsAndMatches: $molecularTestsAndMatches")

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
        }.distinctBy { it.event } // als er meerdere evidence is zoals voor irinotectan / braf v600e, wordt alleen 1e gepakt?
    }

    fun isFound(evidence: EfficacyEvidence, molecularHistory: MolecularHistory): Boolean? {
        val molecularTestsAndMatches = molecularHistory.molecularTests.map { it to actionabilityMatcher.match(it) }

        with(evidence.molecularCriterium()) {
            return when {
                hotspots().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.variants.any { hasEvidence(it, molecularTest.second) }
                    }
                }

                codons().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.variants.any { hasEvidence(it, molecularTest.second) }
                    }
                }

                exons().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.variants.any { hasEvidence(it, molecularTest.second) }
                    }
                }

                genes().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        with(molecularTest.first.drivers) {
                            val variantMatch = variants.any { hasEvidence(it, molecularTest.second) }
                            val fusionMatch = fusions.any { hasEvidence(it, molecularTest.second) }
                            variantMatch || fusionMatch ||
                                    copyNumbers.any { hasEvidence(it, molecularTest.second) } ||
                                    homozygousDisruptions.any { hasEvidence(it, molecularTest.second) } ||
                                    disruptions.any { hasEvidence(it, molecularTest.second) }
                        }
                    }
                }

                fusions().isNotEmpty() -> {
                    molecularTestsAndMatches.any { molecularTest ->
                        molecularTest.first.drivers.fusions.any {
                            hasEvidence(it, molecularTest.second)
                        }
                    }
                }

                // TODO (CB): Also look for resistance for HLA and characteristics?
                else -> null
            }
        }
    }

    private fun hasEvidence(it: Actionable, matches: MatchesForActionable) = matches[it]?.evidenceMatches?.isNotEmpty() == true

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
            molecularHistory: MolecularHistory,
            actionabilityMatcher: ActionabilityMatcher
        ): ResistanceEvidenceMatcher {
            val expandedTumorDoids = expandDoids(doidModel, tumorDoids)
            val onLabelNonPositiveEvidence = evidences.filter { hasNoPositiveResponse(it) && isOnLabel(it, expandedTumorDoids) }

            return ResistanceEvidenceMatcher(
                onLabelNonPositiveEvidence,
                treatmentDatabase,
                actionabilityMatcher,
                molecularHistory
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