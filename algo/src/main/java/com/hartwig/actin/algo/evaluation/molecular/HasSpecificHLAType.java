package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;

import org.jetbrains.annotations.NotNull;

public class HasSpecificHLAType implements EvaluationFunction {

    @NotNull
    private final String hlaAlleleToFind;

    HasSpecificHLAType(@NotNull final String hlaAlleleToFind) {
        this.hlaAlleleToFind = hlaAlleleToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        MolecularImmunology immunology = record.molecular().immunology();

        if (!immunology.isReliable()) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("HLA typing has not been performed reliably")
                    .addUndeterminedGeneralMessages("HLA typing")
                    .build();
        }

        for (HlaAllele hlaAllele : immunology.hlaAlleles()) {
            if (hlaAllele.name().equals(hlaAlleleToFind)) {
                boolean alleleIsPresentInTumor = hlaAllele.tumorCopyNumber() >= 0.5;
                boolean alleleHasSomaticMutations = hlaAllele.hasSomaticMutations();

                if (alleleIsPresentInTumor && !alleleHasSomaticMutations) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has HLA type " + hlaAllele.name() + " which is equal to required allele type "
                                    + hlaAlleleToFind + ", this allele is present and without somatic variants in tumor")
                            .addPassGeneralMessages("HLA type")
                            .addInclusionMolecularEvents(hlaAllele.name())
                            .build();
                } else {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.WARN)
                            .addWarnSpecificMessages("Patient has HLA type " + hlaAllele.name() + " which is equal to required allele type "
                                    + hlaAlleleToFind + ", however, this allele is affected in tumor.")
                            .addWarnGeneralMessages("HLA type")
                            .addInclusionMolecularEvents(hlaAllele.name())
                            .build();
                }
            }
        }
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have HLA type '" + hlaAlleleToFind + "'")
                .addFailGeneralMessages("HLA typing")
                .build();
    }
}
