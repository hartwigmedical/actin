package com.hartwig.actin.clinical.datamodel.treatment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DrugType implements TreatmentType {
    ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ABL inhibitor"),
    ADENOSINE_TARGETING(TreatmentCategory.IMMUNOTHERAPY),
    ALK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ALK inhibitor"),
    ALKYLATING_AGENT(TreatmentCategory.CHEMOTHERAPY),
    ANTHRACYCLINE(TreatmentCategory.CHEMOTHERAPY),
    ANTI_ANDROGEN(TreatmentCategory.HORMONE_THERAPY, "anti-androgen"),
    ANTI_CD3(TreatmentCategory.TARGETED_THERAPY, "anti-CD3"),
    ANTI_CD9(TreatmentCategory.TARGETED_THERAPY, "anti-CD9"),
    ANTI_CD40(TreatmentCategory.TARGETED_THERAPY, "anti-CD40"),
    ANTI_CD137(TreatmentCategory.IMMUNOTHERAPY, "anti-CD137"),
    ANTI_CEACAM5(TreatmentCategory.TARGETED_THERAPY, "anti-CEACAM5"),
    ANTI_CLDN6_CAR_T(TreatmentCategory.CAR_T, "anti-CLDN6"),
    ANTI_ESTROGEN(TreatmentCategory.HORMONE_THERAPY, "anti-estrogen"),
    ANTI_KLK2(TreatmentCategory.TARGETED_THERAPY, "anti-KLK2"),
    ANTI_OX40(TreatmentCategory.IMMUNOTHERAPY, "anti-OX40"),
    ANTI_PD_1(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-1"),
    ANTI_PD_1_TIM_3(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-1/TIM-3"),
    ANTI_PD_1_LAG_3(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-1/LAG-3"),
    ANTI_PD_L1(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-L1"),
    ANTI_PD_L2(TreatmentCategory.IMMUNOTHERAPY, "anti-PD-L2"),
    ANTI_TIGIT(TreatmentCategory.IMMUNOTHERAPY, "anti-TIGIT"),
    ANTI_TISSUE_FACTOR(TreatmentCategory.TARGETED_THERAPY, "anti tissue-factor (TF)"),
    ANTIBODY_DRUG_CONJUGATE_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY, "antibody-drug conjugate"),
    ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY, "antibody-drug conjugate"),
    ANTIMETABOLITE(TreatmentCategory.CHEMOTHERAPY),
    ANTIMICROTUBULE_AGENT(TreatmentCategory.CHEMOTHERAPY),
    AROMATASE_INHIBITOR(TreatmentCategory.HORMONE_THERAPY),
    AXL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "AXL inhibitor"),
    BCR_ABL_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "BCR-ABL inhibitor"),
    BISPHOSPHONATE(TreatmentCategory.SUPPORTIVE_TREATMENT),
    BRAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "BRAF inhibitor"),
    CALCIUM_REGULATORY_MEDICATION(TreatmentCategory.SUPPORTIVE_TREATMENT),
    CD73_ANTIBODY(TreatmentCategory.IMMUNOTHERAPY, "CD73 antibody"),
    CDK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK inhibitor"),
    CDK2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK2 inhibitor"),
    CDK4_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK4 inhibitor"),
    CDK4_6_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK4/6 inhibitor"),
    CDK6_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CDK6 inhibitor"),
    CORTICOSTEROID(TreatmentCategory.SUPPORTIVE_TREATMENT),
    CSF1R_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "CSF1R inhibitor"),
    CTLA4_INHIBITOR(TreatmentCategory.IMMUNOTHERAPY, "CTLA4 inhibitor"),
    CYTOKINE(TreatmentCategory.IMMUNOTHERAPY),
    DDR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "DDR1 inhibitor"),
    DDR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "DDR2 inhibitor"),
    EGFR_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "EGFR antibody"),
    ERBB2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ERBB2 inhibitor"),
    FGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR inhibitor"),
    FGFR1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR1 inhibitor"),
    FGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR2 inhibitor"),
    FGFR3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FGFR3 inhibitor"),
    FLT3_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FLT3 inhibitor"),
    FLT3L_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "FLT3L inhibitor"),
    FLUOROPYRIMIDINE(TreatmentCategory.CHEMOTHERAPY),
    GAMMA_SECRETASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    GONADORELIN_AGONIST(TreatmentCategory.HORMONE_THERAPY),
    HEDGEHOG_PATHWAY_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    HER2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "HER2 antibody"),
    HER2_CAR_T(TreatmentCategory.CAR_T, "HER2 antibody"),
    HER2_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY, "HER2 antibody"),
    HPV_VACCINE(TreatmentCategory.IMMUNOTHERAPY, "HPV vaccine"),
    HPV16_VACCINE(TreatmentCategory.IMMUNOTHERAPY, "HPV-16 vaccine"),
    HORMONE_ANTINEOPLASTIC(TreatmentCategory.HORMONE_THERAPY),
    IDO1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "IDO1 inhibitor"),
    IL_2_CYTOKINE(TreatmentCategory.IMMUNOTHERAPY, "IL-2 cytokine"),
    IL_15_CYTOKINE(TreatmentCategory.IMMUNOTHERAPY, "IL-15 cytokine"),
    IMMUNE_CHECKPOINT_INHIBITOR(TreatmentCategory.IMMUNOTHERAPY),
    KIT_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "KIT inhibitor"),
    MEK_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MEK inhibitor"),
    MET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MET inhibitor"),
    MONOCLONAL_ANTIBODY_IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY, "monoclonal antibody - immunotherapies"),
    MONOCLONAL_ANTIBODY_SUPPORTIVE_TREATMENT(TreatmentCategory.SUPPORTIVE_TREATMENT, "monoclonal antibody - supportive treatments"),
    MONOCLONAL_ANTIBODY_TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY, "monoclonal antibody - targeted therapies"),
    MTORC1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "MTORC1 inhibitor"),
    NONSTEROIDAL_ANTI_ANDROGEN(TreatmentCategory.HORMONE_THERAPY),
    NOTCH_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    NOVEL_HORMONE_THERAPY_FOR_PROSTATE(TreatmentCategory.HORMONE_THERAPY),
    PARP_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PARP inhibitor"),
    PD_1_PD_L1_ANTIBODY(TreatmentCategory.IMMUNOTHERAPY, "PD-1/PD-L1 antibody"),
    PDGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFR inhibitor"),
    PDGFRA_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFRA inhibitor"),
    PDGFRB_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PDGFRB inhibitor"),
    PIK3CA_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "PIK3CA inhibitor"),
    PLATINUM_COMPOUND(TreatmentCategory.CHEMOTHERAPY),
    PROGESTIN(TreatmentCategory.HORMONE_THERAPY),
    PROTEIN_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    PYRIMIDINE_ANTAGONIST(TreatmentCategory.CHEMOTHERAPY),
    RAF_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RAF inhibitor"),
    RANKL_ANTIBODY(TreatmentCategory.SUPPORTIVE_TREATMENT, "RANKL antibody"),
    RET_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RET inhibitor"),
    RON_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "RON inhibitor"),
    ROS1_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "ROS1 inhibitor"),
    SRC_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "SRC inhibitor"),
    SMO_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "SMO inhibitor"),
    STEROID(TreatmentCategory.SUPPORTIVE_TREATMENT),
    TAXANE(TreatmentCategory.CHEMOTHERAPY),
    THYMIDINE_PHOSPHORYLASE_INHIBITOR(TreatmentCategory.CHEMOTHERAPY),
    TOPO1_INHIBITOR(TreatmentCategory.CHEMOTHERAPY, "TOPO1 inhibitor"),
    TOPO2_INHIBITOR(TreatmentCategory.CHEMOTHERAPY, "TOPO2 inhibitor"),
    TRK_RECEPTOR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "TRK receptor inhibitor"),
    TROP2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "TROP2 antibody"),
    TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY),
    VACCINE(TreatmentCategory.PROPHYLACTIC_TREATMENT),
    VEGF_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "VEGF antibody"),
    VEGFR_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "VEGFR inhibitor"),
    VEGFR2_ANTIBODY(TreatmentCategory.TARGETED_THERAPY, "VEGFR2 antibody"),
    VEGFR2_INHIBITOR(TreatmentCategory.TARGETED_THERAPY, "VEGFR2 inhibitor");

    @NotNull
    private final TreatmentCategory category;
    @Nullable
    private final String display;

    DrugType(@NotNull TreatmentCategory category) {
        this(category, null);
    }

    DrugType(@NotNull TreatmentCategory category, @Nullable String display) {
        this.category = category;
        this.display = display;
    }

    @NotNull
    @Override
    public TreatmentCategory category() {
        return category;
    }

    @NotNull
    @Override
    public String display() {
        return (display != null) ? display : toString().replace("_", " ").toLowerCase();
    }
}