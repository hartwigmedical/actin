package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasSpinalCordCompression implements EvaluationFunction {

    @VisibleForTesting
    static final Set<List<String>> SPINAL_CORD_COMPRESSION_PATTERNS = Sets.newHashSet();

    static {
        SPINAL_CORD_COMPRESSION_PATTERNS.add(Lists.newArrayList("spinal", "cord", "compression"));
        SPINAL_CORD_COMPRESSION_PATTERNS.add(Lists.newArrayList("cervical", "spondylotic", "myelopathy"));
    }

    HasSpinalCordCompression() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> spinalCordCompressions = Sets.newHashSet();
        for (Complication complication : record.clinical().complications()) {
            if (isPotentialSpinalCordCompression(complication.name())) {
                spinalCordCompressions.add(complication.name());
            }
        }

        if (!spinalCordCompressions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has spinal cord compression " + Format.concat(spinalCordCompressions))
                    .addPassGeneralMessages(Format.concat(spinalCordCompressions))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have spinal cord compression")
                .build();
    }

    private static boolean isPotentialSpinalCordCompression(@NotNull String complication) {
        return PatternMatcher.isMatch(complication, SPINAL_CORD_COMPRESSION_PATTERNS);
    }
}
