package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.MolecularInterpreter
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion
import com.hartwig.actin.molecular.datamodel.WGSMolecularTest
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.orange.ExperimentType
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.hmftools.datamodel.purple.PurpleQCStatus

class OrangeInterpreter(private val geneFilter: GeneFilter) : MolecularInterpreter<OrangeRecord, MolecularRecord> {

    override fun interpret(records: List<OrangeRecord>): List<MolecularTest<MolecularRecord>> {
        return records.map { record ->
            validateOrangeRecord(record)
            val driverExtractor: DriverExtractor = DriverExtractor.create(geneFilter)
            val characteristicsExtractor = CharacteristicsExtractor()

            MolecularRecord(
                patientId = toPatientId(record.sampleId()),
                sampleId = record.sampleId(),
                type = determineExperimentType(record.experimentType()),
                refGenomeVersion = determineRefGenomeVersion(record.refGenomeVersion()),
                date = record.samplingDate(),
                evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
                externalTrialSource = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display(),
                containsTumorCells = containsTumorCells(record),
                hasSufficientQualityAndPurity = hasSufficientQualityAndPurity(record),
                hasSufficientQuality = hasSufficientQuality(record),
                characteristics = characteristicsExtractor.extract(record),
                drivers = driverExtractor.extract(record),
                immunology = ImmunologyExtraction.extract(record),
                pharmaco = PharmacoExtraction.extract(record)
            )
        }.map { WGSMolecularTest(type = it.type, date = it.date, result = it) }
    }

    companion object {
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

        fun containsTumorCells(record: OrangeRecord): Boolean {
            return PurpleQCStatus.FAIL_NO_TUMOR !in record.purple().fit().qc().status()
        }

        fun hasSufficientQualityAndPurity(record: OrangeRecord): Boolean {
            return recordQCStatusesInSet(record, setOf(PurpleQCStatus.PASS))
        }

        fun hasSufficientQuality(record: OrangeRecord): Boolean {
            return recordQCStatusesInSet(record, setOf(PurpleQCStatus.PASS, PurpleQCStatus.WARN_LOW_PURITY))
        }

        private fun recordQCStatusesInSet(record: OrangeRecord, allowableQCStatuses: Set<PurpleQCStatus>): Boolean {
            return allowableQCStatuses.containsAll(record.purple().fit().qc().status())
        }

        internal fun toPatientId(sampleId: String): String {
            require(sampleId.length >= 12) { "Cannot convert sampleId to patientId: $sampleId" }
            return sampleId.substring(0, 12)
        }

        fun determineExperimentType(experimentType: ExperimentType?): com.hartwig.actin.molecular.datamodel.ExperimentType {
            return when (experimentType) {
                ExperimentType.TARGETED -> {
                    com.hartwig.actin.molecular.datamodel.ExperimentType.TARGETED
                }

                ExperimentType.WHOLE_GENOME -> {
                    com.hartwig.actin.molecular.datamodel.ExperimentType.WHOLE_GENOME
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
                ("must be null or empty because ACTIN only accepts ORANGE output that has been " + "scrubbed of germline data. Please use the JSON output from the 'orange_no_germline' directory.")
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
}
