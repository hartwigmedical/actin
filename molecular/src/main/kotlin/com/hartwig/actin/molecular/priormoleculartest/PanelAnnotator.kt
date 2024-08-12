package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.hartwig.actin.tools.variant.VariantAnnotator as VariantResolver

private const val TMB_HIGH_CUTOFF = 10.0

class PanelAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    private val variantResolver: VariantResolver,
    private val paver: Paver,
    private val paveLite: PaveLite,
    private val knownFusionCache: KnownFusionCache,
    private val ensembleDataCache: EnsemblDataCache
) :
    MolecularAnnotator<PanelExtraction, PanelRecord> {

    override fun annotate(input: PanelExtraction): PanelRecord {
        val annotatedVariants = PanelVariantAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantResolver, paver, paveLite)
            .annotate(input.variants)
        val annotatedAmplifications = input.amplifications.map(::inferredCopyNumber).map(::annotatedInferredCopyNumber)
        val annotatedFusions =
            PanelFusionAnnotator(evidenceDatabase, knownFusionCache, ensembleDataCache).annotate(input.fusions, input.skippedExons)

        return PanelRecord(
            panelExtraction = input,
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = input.panelType,
            date = input.date,
            drivers = Drivers(
                variants = annotatedVariants,
                copyNumbers = annotatedAmplifications.toSet(),
                fusions = annotatedFusions,
            ),
            characteristics = MolecularCharacteristics(
                isMicrosatelliteUnstable = input.isMicrosatelliteUnstable,
                tumorMutationalBurden = input.tumorMutationalBurden,
                hasHighTumorMutationalBurden = input.tumorMutationalBurden?.let { it > TMB_HIGH_CUTOFF }),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }

    private fun annotatedInferredCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber))
        val geneAlteration =
            GeneAlterationFactory.convertAlteration(copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber))
        return copyNumber.copy(
            evidence = evidence,
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )
    }

    private fun inferredCopyNumber(panelAmplificationExtraction: PanelAmplificationExtraction) = CopyNumber(
        gene = panelAmplificationExtraction.gene,
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null,
        isReportable = true,
        event = panelAmplificationExtraction.display(),
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = ActionableEvidenceFactory.createNoEvidence(),
        type = CopyNumberType.FULL_GAIN,
        minCopies = 6,
        maxCopies = 6
    )

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PanelAnnotator::class.java)
    }
}