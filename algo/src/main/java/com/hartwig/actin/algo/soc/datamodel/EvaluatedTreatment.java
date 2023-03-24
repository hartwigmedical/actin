package com.hartwig.actin.algo.soc.datamodel;

import java.util.List;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.immutables.value.Value;

@Value.Immutable
public abstract class EvaluatedTreatment {

    public abstract Treatment treatment();

    public abstract List<Evaluation> evaluations();

    public abstract int score();
}
