package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.efficacy.Treatment as ServeTreatment

class ResistanceEvidenceMatcher(
    private val candidateEvidences: List<EfficacyEvidence>,
    private val treatmentDatabase: TreatmentDatabase,
    // TODO (CB): Use clinicalEvidenceMatcher to generate all matches and then simplify this function?
    @Suppress("unused") private val actionabilityMatcher: ActionabilityMatcher,
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

        with(evidence.molecularCriterium()) {
            return when {
                hotspots().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any { hasEvidence(it) }
                    }
                }

                codons().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any { hasEvidence(it) }
                    }
                }

                exons().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any { hasEvidence(it) }
                    }
                }

                genes().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        with(molecularTest.drivers) {
                            val variantMatch = variants.any { hasEvidence(it) }
                            val fusionMatch = fusions.any { hasEvidence(it) }
                            variantMatch || fusionMatch ||
                                    copyNumbers.any { hasEvidence(it) } ||
                                    homozygousDisruptions.any { hasEvidence(it) } ||
                                    disruptions.any { hasEvidence(it) }
                        }
                    }
                }

                fusions().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.fusions.any {
                            hasEvidence(it, molecularTest)
                        }
                    }
                }

                // TODO (CB): Also look for resistance for HLA and characteristics?
                else -> null
            }
        }
    }

    private fun hasEvidence(it: Actionable, molecularTest: MolecularTest) =
        actionabilityMatcher.match(molecularTest)[it]?.evidenceMatches?.isNotEmpty() == true

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
            molecularHistory: MolecularHistory,
            serveRecord: ServeRecord
        ): ResistanceEvidenceMatcher {
            val expandedTumorDoids = expandDoids(doidModel, tumorDoids)
            val onLabelNonPositiveEvidence = evidences.filter { hasNoPositiveResponse(it) && isOnLabel(it, expandedTumorDoids) }


            return ResistanceEvidenceMatcher(
                onLabelNonPositiveEvidence,
                treatmentDatabase,
                ActionabilityMatcher(serveRecord.evidences(), serveRecord.trials()),
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