package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.actionability.BreakendEvidence
import com.hartwig.actin.molecular.evidence.actionability.CopyNumberEvidence
import com.hartwig.actin.molecular.evidence.actionability.FusionEvidence
import com.hartwig.actin.molecular.evidence.actionability.HomozygousDisruptionEvidence
import com.hartwig.actin.molecular.evidence.actionability.VariantEvidence
import com.hartwig.actin.molecular.evidence.orange.MolecularRecordAnnotatorFunctions
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel

class ResistanceEvidenceMatcher(
    private val candidateEvidences: List<EfficacyEvidence>,
    private val treatmentDatabase: TreatmentDatabase,
    private val molecularHistory: MolecularHistory
) {

    fun match(treatment: Treatment): List<ResistanceEvidence> {
        return candidateEvidences.mapNotNull { evidence ->
            findTreatmentInDatabase(evidence.treatment(), treatment)?.let { treatmentName ->
                ResistanceEvidence(
                    event = findSourceEvent(evidence),
                    isTested = null,
                    isFound = isFound(evidence, molecularHistory),
                    resistanceLevel = evidence.evidenceLevel().toString(),
                    evidenceUrls = evidence.urls(),
                    treatmentName = treatmentName
                )
            }
        }.distinctBy { it.event }
    }

    fun isFound(evidence: EfficacyEvidence, molecularHistory: MolecularHistory): Boolean? {
        val molecularTests = molecularHistory.molecularTests
        val variantEvidence = VariantEvidence.create(evidences = listOf(evidence), trials = emptyList())

        with(evidence.molecularCriterium()) {
            return when {
                hotspots().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any {
                            val criteria = MolecularRecordAnnotatorFunctions.createVariantCriteria(it)
                            variantEvidence.findMatches(criteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                genes().isNotEmpty() -> {
                    val fusionEvidence = FusionEvidence.create(evidences = listOf(evidence), trials = emptyList())
                    val copyNumberEvidence = CopyNumberEvidence.create(evidences = listOf(evidence), trials = emptyList())
                    val homDisEvidence = HomozygousDisruptionEvidence.create(evidences = listOf(evidence), trials = emptyList())
                    val disruptionEvidence = BreakendEvidence.create(evidences = listOf(evidence), trials = emptyList())

                    molecularTests.any { molecularTest ->
                        with(molecularTest.drivers) {
                            val variantMatch = variants.any {
                                val criteria = MolecularRecordAnnotatorFunctions.createVariantCriteria(it)
                                variantEvidence.findMatches(criteria).evidenceMatches.isNotEmpty()
                            }
                            val fusionMatch = fusions.any {
                                val criteria = MolecularRecordAnnotatorFunctions.createFusionCriteria(it)
                                fusionEvidence.findMatches(criteria).evidenceMatches.isNotEmpty()
                            }
                            variantMatch || fusionMatch ||
                                    copyNumbers.any { copyNumberEvidence.findMatches(it).evidenceMatches.isNotEmpty() } ||
                                    homozygousDisruptions.any { homDisEvidence.findMatches(it).evidenceMatches.isNotEmpty() } ||
                                    disruptions.any { disruptionEvidence.findMatches(it).evidenceMatches.isNotEmpty() }
                        }
                    }
                }

                fusions().isNotEmpty() -> {
                    val fusionEvidence = FusionEvidence.create(evidences = listOf(evidence), trials = emptyList())
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.fusions.any {
                            val criteria = MolecularRecordAnnotatorFunctions.createFusionCriteria(it)
                            fusionEvidence.findMatches(criteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                exons().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any {
                            val criteria = MolecularRecordAnnotatorFunctions.createVariantCriteria(it)
                            variantEvidence.findMatches(criteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                codons().isNotEmpty() -> {
                    molecularTests.any { molecularTest ->
                        molecularTest.drivers.variants.any {
                            val criteria = MolecularRecordAnnotatorFunctions.createVariantCriteria(it)
                            variantEvidence.findMatches(criteria).evidenceMatches.isNotEmpty()
                        }
                    }
                }

                else -> null
            }
        }
    }

    private fun findTreatmentInDatabase(intervention: com.hartwig.serve.datamodel.efficacy.Treatment, treatmentToFind: Treatment): String? {
        return EfficacyEntryFactory(treatmentDatabase).generateOptions(listOf(intervention.name()))
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

    companion object {
        fun create(
            doidModel: DoidModel,
            tumorDoids: Set<String>,
            evidences: List<EfficacyEvidence>,
            treatmentDatabase: TreatmentDatabase,
            molecularHistory: MolecularHistory
        ): ResistanceEvidenceMatcher {
            val expandedTumorDoids = expandDoids(doidModel, tumorDoids)
            val filteredActionableEvents = evidences.filter { hasNoPositiveResponse(it) && isOnLabel(it, expandedTumorDoids) }

            return ResistanceEvidenceMatcher(filteredActionableEvents, treatmentDatabase, molecularHistory)
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

        private fun findSourceEvent(event: EfficacyEvidence): String {
            // Assumes there is no combined/complex evidence yet
            with(event.molecularCriterium()) {
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
    }
}