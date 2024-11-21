package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
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
import com.hartwig.serve.datamodel.molecular.ActionableEvent

class ResistanceEvidenceMatcher(
    private val doidModel: DoidModel,
    private val applicableDoids: Set<String>,
    private val actionableEvents: List<EfficacyEvidence>,
    private val treatmentDatabase: TreatmentDatabase,
    private val molecularHistory: MolecularHistory
) {

    fun match(treatment: Treatment): List<ResistanceEvidence> {
//        val allActionableEvents =
//            actionableEvents.hotspots() + actionableEvents.codons() + actionableEvents.exons() + actionableEvents.genes() +
//                    actionableEvents.fusions() + actionableEvents.hla() + actionableEvents.characteristics()
        //return findMatches(allActionableEvents, treatment)
        return emptyList()
    }

//    private fun findMatches(actionableEvents: List<ActionableEvent>, treatment: Treatment): List<ResistanceEvidence> {
//        val expandedTumorDoids = expandDoids(doidModel, applicableDoids)
//        return actionableEvents.filter {
//            hasNoPositiveResponse(it) &&
//            isOnLabel(it, expandedTumorDoids) &&
//                    findTreatmentInDatabase(it.intervention(), treatment) != null
//        }.map { actionableEvent ->
//            ResistanceEvidence(
//                event = actionableEvent.sourceEvent(),
//                isTested = null,
//                isFound = isFound(actionableEvent, molecularHistory),
//                resistanceLevel = actionableEvent.evidenceLevel().toString(),
//                evidenceUrls = actionableEvent.evidenceUrls(),
//                treatmentName = findTreatmentInDatabase(actionableEvent.intervention(), treatment)!!
//            )
//        }.distinctBy { it.event }
//    }

//    fun isFound(event: ActionableEvent, molecularHistory: MolecularHistory): Boolean? {
//        val molecularTests = molecularHistory.molecularTests
//
//        when (event) {
//            is ActionableHotspot -> {
//                val variantEvidence = VariantEvidence(listOf(event), emptyList(), emptyList())
//                return molecularTests.any { molecularTest ->
//                    molecularTest.drivers.variants.any {
//                        variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty()
//                    }
//                }
//            }
//
//            is ActionableGene -> {
//                val actionableEvents = ImmutableActionableEvents.builder().genes(listOf(event)).build()
//
//                val variantEvidence = VariantEvidence.create(actionableEvents)
//                val fusionEvidence = FusionEvidence.create(actionableEvents)
//                val copyNumberEvidence = CopyNumberEvidence.create(actionableEvents)
//                val homDisEvidence = HomozygousDisruptionEvidence.create(actionableEvents)
//                val disruptionEvidence = BreakendEvidence.create(actionableEvents)
//
//                val variantMatch =
//                    molecularTests.any { molecularTest ->
//                        molecularTest.drivers.variants.any {
//                            variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty()
//                        }
//                    }
//                val fusionMatch = molecularTests.any { molecularTest ->
//                    molecularTest.drivers.fusions.any {
//                        fusionEvidence.findMatches(MolecularRecordAnnotatorFunctions.createFusionCriteria(it)).isNotEmpty()
//                    }
//                }
//                val copyNumberMatch = molecularTests.any { molecularTest ->
//                    molecularTest.drivers.copyNumbers.any {
//                        copyNumberEvidence.findMatches(it).isNotEmpty()
//                    }
//                }
//                val homDisMatch = molecularTests.any { molecularTest ->
//                    molecularTest.drivers.homozygousDisruptions.any {
//                        homDisEvidence.findMatches(it).isNotEmpty()
//                    }
//                }
//                val disruptionMatch = molecularTests.any { molecularTest ->
//                    molecularTest.drivers.disruptions.any {
//                        disruptionEvidence.findMatches(it).isNotEmpty()
//                    }
//                }
//
//                return variantMatch || fusionMatch || copyNumberMatch || homDisMatch || disruptionMatch
//
//            }
//
//            is ActionableFusion -> {
//                val fusionEvidence = FusionEvidence(emptyList(), listOf(event))
//                return molecularTests.any { molecularTest ->
//                    molecularTest.drivers.fusions.any {
//                        fusionEvidence.findMatches(MolecularRecordAnnotatorFunctions.createFusionCriteria(it)).isNotEmpty()
//                    }
//                }
//            }
//
//            is ActionableRange -> {
//                val variantEvidence = VariantEvidence(emptyList(), listOf(event), emptyList())
//                return molecularTests.any { molecularTest ->
//                    molecularTest.drivers.variants.any {
//                        variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty()
//                    }
//                }
//            }
//
//            is ActionableCharacteristic -> {
//                return null
//            }
//
//            is ActionableHLA -> {
//                return null
//            }
//
//            else -> {
//                return null
//            }
//        }
//    }

//    private fun findTreatmentInDatabase(intervention: Intervention, treatmentToFind: Treatment): String? {
//        val therapyName = (intervention as com.hartwig.serve.datamodel.Treatment).name()
//        val treatment = EfficacyEntryFactory(treatmentDatabase).generateOptions(listOf(therapyName))
//            .mapNotNull(treatmentDatabase::findTreatmentByName)
//            .distinct().singleOrNull()
//        return if (treatment?.let { drugsInOtherTreatment(treatmentToFind, treatment) } == true) treatment.name
//        else null
//    }
//
//    private fun isOnLabel(event: ActionableEvent, expandedTumorDoids: Set<String>): Boolean {
//        if (!expandedTumorDoids.contains(event.applicableCancerType().doid())) {
//            return false
//        }
//        return event.blacklistCancerTypes().none { expandedTumorDoids.contains(it.doid()) }
//    }
//
//    private fun expandDoids(doidModel: DoidModel, doids: Set<String>): Set<String> {
//        return doids.flatMap { doidModel.doidWithParents(it) }.toSet()
//    }
//
//    private fun hasNoPositiveResponse(resistanceEvent: ActionableEvent): Boolean {
//        return resistanceEvent.evidenceLevel() in setOf(
//            EvidenceLevel.A,
//            EvidenceLevel.B,
//            EvidenceLevel.C
//        ) && !resistanceEvent.direction().hasPositiveResponse()
//    }

//    private fun drugsInOtherTreatment(treatment1: Treatment, treatment2: Treatment): Boolean {
//        val drugs1 = (treatment1 as DrugTreatment).drugs
//        val drugs2 = (treatment2 as DrugTreatment).drugs
//        return (drugs2.all { it in drugs1 })
//    }

    companion object {
        fun create(
            doidModel: DoidModel,
            tumorDoids: Set<String>,
            actionableEvents: List<EfficacyEvidence>,
            treatmentDatabase: TreatmentDatabase,
            molecularHistory: MolecularHistory
        ): ResistanceEvidenceMatcher {
            return ResistanceEvidenceMatcher(doidModel, tumorDoids, actionableEvents, treatmentDatabase, molecularHistory)
        }
    }
}