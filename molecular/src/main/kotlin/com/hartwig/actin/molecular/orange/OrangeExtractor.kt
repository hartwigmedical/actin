package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.panel.PanelTestSpecification
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.panel.PanelSpecifications
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.hmftools.datamodel.purple.PurpleQCStatus
import java.time.LocalDate
import com.hartwig.hmftools.datamodel.orange.ExperimentType as OrangeExperimentType

private const val ONCO_PANEL = "OncoPanel"

class OrangeExtractor(private val geneFilter: GeneFilter, private val panelSpecifications: PanelSpecifications) :
    MolecularExtractor<OrangeRecord, MolecularTest> {

    override fun extract(input: List<OrangeRecord>): List<MolecularTest> {
        return input.map(::interpret)
    }

    fun interpret(record: OrangeRecord): MolecularTest {
        validateOrangeRecord(record)
        val driverExtractor = DriverExtractor.create(geneFilter)

        return MolecularTest(
            date = record.samplingDate(),
            sampleId = record.sampleId(),
            reportHash = null,
            experimentType = determineExperimentType(record.experimentType()),
            testTypeDisplay = null,
            targetSpecification = if (record.experimentType() == OrangeExperimentType.TARGETED) {
                panelSpecifications.panelTargetSpecification(PanelTestSpecification(ONCO_PANEL, LocalDate.of(2024, 12, 9)), null)
            } else null,
            refGenomeVersion = determineRefGenomeVersion(record.refGenomeVersion()),
            containsTumorCells = containsTumorCells(record),
            hasSufficientPurity = hasSufficientPurity(record),
            hasSufficientQuality = hasSufficientQuality(record),
            isContaminated = isContaminated(record),
            characteristics = CharacteristicsExtraction.extract(record),
            drivers = driverExtractor.extract(record),
            immunology = ImmunologyExtraction.extract(record),
            pharmaco = PharmacoExtraction.extract(record),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            externalTrialSource = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display(),
        )
    }

    fun determineRefGenomeVersion(refGenomeVersion: OrangeRefGenomeVersion): RefGenomeVersion {
        return when (refGenomeVersion) {
            OrangeRefGenomeVersion.V37 -> {
                RefGenomeVersion.V37
            }

            OrangeRefGenomeVersion.V38 -> {
                RefGenomeVersion.V38
            }
        }
    }

    fun hasSufficientQuality(record: OrangeRecord): Boolean {
        return containsTumorCells(record) && !isContaminated(record)
    }

    fun hasSufficientPurity(record: OrangeRecord): Boolean {
        return PurpleQCStatus.WARN_LOW_PURITY !in record.purple().fit().qc().status() && containsTumorCells(record)
    }

    private fun containsTumorCells(record: OrangeRecord): Boolean {
        return PurpleQCStatus.FAIL_NO_TUMOR !in record.purple().fit().qc().status()
    }

    private fun isContaminated(record: OrangeRecord): Boolean {
        return PurpleQCStatus.FAIL_CONTAMINATION in record.purple().fit().qc().status()
    }

    private fun determineExperimentType(experimentType: OrangeExperimentType?): ExperimentType {
        return when (experimentType) {
            OrangeExperimentType.TARGETED -> {
                ExperimentType.HARTWIG_TARGETED
            }

            OrangeExperimentType.WHOLE_GENOME -> {
                ExperimentType.HARTWIG_WHOLE_GENOME
            }

            null -> throw IllegalStateException("Experiment type is required but was null")
        }
    }

    private fun validateOrangeRecord(orange: OrangeRecord) {
        throwIfGermlineFieldNonEmpty(orange)
        throwIfAnyCuppaPredictionClassifierMissing(orange)
        throwIfPurpleQCMissing(orange)
    }

    private fun throwIfGermlineFieldNonEmpty(orange: OrangeRecord) {
        val message =
            ("must be null or empty because ACTIN only accepts ORANGE output that has been " +
                    "scrubbed of germline data. Please use the JSON output from the 'orange_no_germline' directory.")

        val allGermlineVariants = orange.purple().allGermlineVariants()
        check(allGermlineVariants.isNullOrEmpty()) { "allGermlineVariants $message" }

        val allGermlineStructuralVariants = orange.linx().allGermlineStructuralVariants()
        check(allGermlineStructuralVariants.isNullOrEmpty()) { "allGermlineStructuralVariants $message" }

        val allGermlineBreakends = orange.linx().allGermlineBreakends()
        check(allGermlineBreakends.isNullOrEmpty()) { "allGermlineBreakends $message" }

        val germlineHomozygousDisruptions = orange.linx().germlineHomozygousDisruptions()
        check(germlineHomozygousDisruptions.isNullOrEmpty()) { "germlineHomozygousDisruptions $message" }
    }

    private fun throwIfAnyCuppaPredictionClassifierMissing(orange: OrangeRecord) {
        val cuppaData = orange.cuppa()
        if (cuppaData != null) {
            for (prediction in cuppaData.predictions()) {
                throwIfCuppaPredictionClassifierMissing(prediction)
            }
        }
    }

    private fun throwIfCuppaPredictionClassifierMissing(prediction: CuppaPrediction) {
        val message = "Missing field %s: cuppa not run in expected configuration"
        checkNotNull(prediction.snvPairwiseClassifier()) { String.format(message, "snvPairwiseClassifer") }
        checkNotNull(prediction.genomicPositionClassifier()) { String.format(message, "genomicPositionClassifier") }
        checkNotNull(prediction.featureClassifier()) { String.format(message, "featureClassifier") }
    }

    private fun throwIfPurpleQCMissing(orange: OrangeRecord) {
        check(orange.purple().fit().qc().status().isNotEmpty()) { "Cannot interpret purple record with empty QC states" }
    }
}