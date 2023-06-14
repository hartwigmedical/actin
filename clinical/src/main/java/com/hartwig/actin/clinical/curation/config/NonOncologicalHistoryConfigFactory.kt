package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationValidator
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.util.ResourceFile
import org.apache.logging.log4j.LogManager
import java.util.Optional

class NonOncologicalHistoryConfigFactory(private val curationValidator: CurationValidator) :
    CurationConfigFactory<NonOncologicalHistoryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): NonOncologicalHistoryConfig {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        return NonOncologicalHistoryConfig(
            input = input,
            ignore = ignore,
            lvef = if (!ignore) toCuratedLVEF(fields, parts) else Optional.empty(),
            priorOtherCondition = if (!ignore) toCuratedPriorOtherCondition(fields, input, parts) else Optional.empty()
        )
    }

    private fun toCuratedPriorOtherCondition(fields: Map<String, Int>, input: String, parts: Array<String>): Optional<PriorOtherCondition> {
        return if (!isLVEF(fields, parts)) {
            val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
            if (!curationValidator.isValidDiseaseDoidSet(doids)) {
                LOGGER.warn("Non-oncological history config with input '$input' contains at least one invalid doid: '$doids'")
            }
            Optional.of(
                ImmutablePriorOtherCondition.builder()
                    .name(parts[fields["name"]!!])
                    .year(ResourceFile.optionalInteger(parts[fields["year"]!!]))
                    .month(ResourceFile.optionalInteger(parts[fields["month"]!!]))
                    .doids(doids)
                    .category(parts[fields["category"]!!])
                    .isContraindicationForTherapy(ResourceFile.bool(parts[fields["isContraindicationForTherapy"]!!]))
                    .build()
            )
        } else {
            Optional.empty()
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(NonOncologicalHistoryConfigFactory::class.java)

        private fun toCuratedLVEF(fields: Map<String, Int>, parts: Array<String>): Optional<Double> {
            return if (isLVEF(fields, parts)) Optional.of(java.lang.Double.valueOf(parts[fields["lvefValue"]!!])) else Optional.empty()
        }

        private fun isLVEF(fields: Map<String, Int>, parts: Array<String>): Boolean {
            return parts[fields["isLVEF"]!!] == "1"
        }
    }
}