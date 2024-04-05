package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry

private const val DPYD_GENE = "DPYD"

class HasHomozygousDPYDDeficiency internal constructor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val pharmaco = record.molecular.pharmaco.filter { it.gene == DPYD_GENE }

        if (pharmaco.isEmpty()) {
            return EvaluationFactory.recoverableUndetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")
        }

        return when {
            containsUnexpectedHaplotypeFunction(pharmaco) -> {
                EvaluationFactory.recoverableUndetermined(
                    "DPYD haplotype function cannot be determined due to unexpected haplotype function",
                    "DPYD haplotype function undetermined"
                )
            }

            isHomozygousDeficient(pharmaco) -> {
                EvaluationFactory.pass("Patient is homozygous DPYD deficient", inclusionEvents = setOf("DPYD deficient"))
            }

            else -> {
                EvaluationFactory.fail("Patient is not homozygous DPYD deficient")
            }
        }
    }

    private fun containsUnexpectedHaplotypeFunction(pharmaco: List<PharmacoEntry>): Boolean {
        return pharmaco.any { pharmacoEntry ->
            pharmacoEntry.haplotypes.any { it.function.lowercase() !in expectedHaplotypeFunctions }
        }
    }

    private fun isHomozygousDeficient(pharmaco: List<PharmacoEntry>): Boolean {
        return pharmaco.none { pharmacoEntry ->
            pharmacoEntry.haplotypes.any { it.function.lowercase() == "normal function" }
        }
    }

    private val expectedHaplotypeFunctions = setOf("normal function", "reduced function", "no function")
}