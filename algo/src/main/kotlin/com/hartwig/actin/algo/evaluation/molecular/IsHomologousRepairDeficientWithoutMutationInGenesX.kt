package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class IsHomologousRepairDeficientWithoutMutationInGenesX(private val genesToFind: Set<String>, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge, false) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val isHRD = test.characteristics.isHomologousRepairDeficient

        with(HomologousRepairDeficiencyGeneSummary.createForDrivers(test.drivers)) {
            val genesToFindWithMutation = genesInGenesToFind(hrdGenesWithBiallelicDriver + hrdGenesWithNonBiallelicDriver)

            return when {
                isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: ${
                            concat(
                                hrdGenesWithBiallelicDriver
                            )
                        } are detected; an HRD test may be recommended",
                        "Unknown HRD status but biallelic drivers in HR genes"
                    )
                }

                isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: ${
                            concat(
                                hrdGenesWithNonBiallelicDriver
                            )
                        } are detected; an HRD test may be recommended",
                        "Unknown HRD status but non-biallelic drivers in HR genes"
                    )
                }

                isHRD == null -> {
                    EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
                }

                isHRD == false -> {
                    EvaluationFactory.fail("No homologous repair deficiency (HRD) detected", "Tumor is not HRD")
                }

                genesToFindWithMutation.isNotEmpty() -> {
                    EvaluationFactory.fail(
                        "Homologous repair deficiency (HRD) detected with variant in ${concat(genesToFindWithMutation)}",
                        "Tumor is HRD with variant in ${concat(genesToFindWithMutation)}"
                    )
                }

                hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) status detected, together with only non-biallelic drivers in HR genes (${
                            concat(
                                hrdGenesWithNonBiallelicDriver
                            )
                        })",
                        "Tumor is HRD (but with only non-biallelic drivers in HR genes)",
                    )
                }

                hrdGenesWithNonBiallelicDriver.isEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) status detected, without drivers in HR genes",
                        "Tumor is HRD (but without detected drivers in HR genes)",
                    )
                }

                else -> {
                    EvaluationFactory.pass(
                        "Homologous repair deficiency (HRD) detected, without variants in ${concat(genesToFind)}",
                        "Tumor is HRD without any variants in ${concat(genesToFind)}",
                    )
                }
            }
        }
    }

    private fun genesInGenesToFind(genes: Iterable<String>): Set<String> {
        return genes.intersect(genesToFind)
    }

}