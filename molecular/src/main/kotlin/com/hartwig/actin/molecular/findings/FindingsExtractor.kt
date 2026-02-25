package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.panel.TestVersion
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.ONCO_PANEL
import com.hartwig.actin.molecular.panel.PanelSpecifications
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.finding.FindingRecord
import com.hartwig.hmftools.datamodel.orange.ExperimentType
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import java.time.LocalDate

class FindingsExtractor(private val geneFilter: GeneFilter, private val panelSpecifications: PanelSpecifications) :
    MolecularExtractor<FindingRecord, MolecularTest> {

    override fun extract(input: List<FindingRecord>): List<MolecularTest> {
        return input.map(::interpret)
    }

    fun interpret(record: FindingRecord): MolecularTest {
        validateFindingRecord(record)
        val driverExtractor = DriverExtractor.Companion.create(geneFilter)
        val metaProperties = record.metaProperties
        return MolecularTest(
            date = record.samplingDate(),
            sampleId = record.sampleId(),
            reportHash = null,
            experimentType = determineExperimentType(metaProperties.experimentType()),
            testTypeDisplay = null,
            targetSpecification = if (metaProperties.experimentType() == ExperimentType.TARGETED) {
                panelSpecifications.panelTargetSpecification(
                    SequencingTest(ONCO_PANEL),
                    TestVersion(LocalDate.of(2024, 12, 9))
                )
            } else null,
            refGenomeVersion = determineRefGenomeVersion(metaProperties.refGenomeVersion()),
            containsTumorCells = containsTumorCells(record),
            hasSufficientPurity = hasSufficientPurity(record),
            hasSufficientQuality = hasSufficientQuality(record),
            isContaminated = isContaminated(record),
            characteristics = CharacteristicsExtraction.extract(record),
            drivers = driverExtractor.extract(record),
            immunology = ImmunologyExtraction.extract(record.hlaAlleles()),
            pharmaco = PharmacoExtraction.extract(record.pharmocoGenotypes()),
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

    fun hasSufficientQuality(record: FindingRecord): Boolean {
        return containsTumorCells(record) && !isContaminated(record)
    }

    fun hasSufficientPurity(record: FindingRecord): Boolean {
        return record.purityPloidyFit().containsTumorCells() && !record.purityPloidyFit().isLowPurity();
    }

    private fun containsTumorCells(record: FindingRecord): Boolean {
        return record.purityPloidyFit().containsTumorCells()
    }

    private fun isContaminated(record: FindingRecord): Boolean {
        return record.purityPloidyFit().isContaminated
    }

    private fun determineExperimentType(experimentType: ExperimentType?): com.hartwig.actin.datamodel.molecular.ExperimentType {
        return when (experimentType) {
            ExperimentType.TARGETED -> {
                com.hartwig.actin.datamodel.molecular.ExperimentType.HARTWIG_TARGETED
            }

            ExperimentType.WHOLE_GENOME -> {
                com.hartwig.actin.datamodel.molecular.ExperimentType.HARTWIG_WHOLE_GENOME
            }

            null -> throw IllegalStateException("Experiment type is required but was null")
        }
    }

    private fun validateFindingRecord(record: FindingRecord) {
        throwIfGermlineFieldNonEmpty(record)
        throwIfAnyCuppaPredictionClassifierMissing(record)
        throwIfPurpleQCMissing(record)
    }

    private fun throwIfGermlineFieldNonEmpty(record: FindingRecord) {
        val message =
            ("must be null or empty because ACTIN only accepts ORANGE output that has been " +
                    "scrubbed of germline data. Please use the JSON output from the 'orange_no_germline' directory.")

        val allGermlineVariants = record.germlineSmallVariants().findings
        check(allGermlineVariants.isNullOrEmpty()) { "allGermlineVariants $message" }

//        val allGermlineStructuralVariants = record.linx().allGermlineStructuralVariants()
//        check(allGermlineStructuralVariants.isNullOrEmpty()) { "allGermlineStructuralVariants $message" }

        val allGermlineBreakends = record.germlineDisruptions().findings
        check(allGermlineBreakends.isNullOrEmpty()) { "allGermlineBreakends $message" }
    }

    private fun throwIfAnyCuppaPredictionClassifierMissing(record: FindingRecord) {
//        val cuppaData = orange.cuppa()
//        if (cuppaData != null) {
//            for (prediction in cuppaData.predictions()) {
//                throwIfCuppaPredictionClassifierMissing(prediction)
//            }
//        }
    }

    private fun throwIfCuppaPredictionClassifierMissing(prediction: CuppaPrediction) {
        val message = "Missing field %s: cuppa not run in expected configuration"
        checkNotNull(prediction.snvPairwiseClassifier()) { String.format(message, "snvPairwiseClassifer") }
        checkNotNull(prediction.genomicPositionClassifier()) { String.format(message, "genomicPositionClassifier") }
        checkNotNull(prediction.featureClassifier()) { String.format(message, "featureClassifier") }
    }

    private fun throwIfPurpleQCMissing(findings: FindingRecord) {
        //check(findings.purple().fit().qc().status().isNotEmpty()) { "Cannot interpret purple record with empty QC states" }
    }
}