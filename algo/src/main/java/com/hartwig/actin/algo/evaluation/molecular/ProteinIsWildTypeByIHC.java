package com.hartwig.actin.algo.evaluation.molecular;

import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class ProteinIsWildTypeByIHC implements EvaluationFunction {

    private static final String IHC = "IHC";
    private final String protein;

    ProteinIsWildTypeByIHC(String protein) {
        this.protein = protein;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasWildTypeResult = record.clinical()
                .priorMolecularTests()
                .stream()
                .filter(test -> test.test().equals(IHC) && test.item().equals(protein))
                .map(test -> Stream.of("wildtype", "wild-type", "wild type").anyMatch(query -> query.equalsIgnoreCase(test.scoreText())))
                .reduce(Boolean::logicalAnd)
                .orElse(false);

        if (hasWildTypeResult) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(String.format("Protein %s is wild type according to IHC", protein))
                    .addPassGeneralMessages(String.format("%s wild type", protein))
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(String.format("Could not determine if protein %s is wild type according to IHC",
                            protein))
                    .addUndeterminedGeneralMessages(String.format("%s wild type status unknown", protein))
                    .build();
        }
    }
}
