package com.hartwig.actin.ckb.datamodel.therapy;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ckb.datamodel.drug.DrugFactory;
import com.hartwig.actin.ckb.datamodel.reference.ReferenceFactory;
import com.hartwig.actin.ckb.json.CkbJsonDatabase;
import com.hartwig.actin.ckb.json.common.GlobalApprovalStatusInfo;
import com.hartwig.actin.ckb.json.common.TherapyInfo;
import com.hartwig.actin.ckb.json.therapy.JsonTherapy;

import org.jetbrains.annotations.NotNull;

public final class TherapyFactory {

    private TherapyFactory() {
    }

    @NotNull
    public static List<Therapy> extractTherapies(@NotNull CkbJsonDatabase ckbJsonDatabase, @NotNull List<TherapyInfo> therapyInfos) {
        List<Therapy> therapies = Lists.newArrayList();
        for (TherapyInfo therapyInfo : therapyInfos) {
            therapies.add(TherapyFactory.resolveTherapy(ckbJsonDatabase, therapyInfo));
        }
        return therapies;
    }

    @NotNull
    public static Therapy resolveTherapy(@NotNull CkbJsonDatabase ckbJsonDatabase, @NotNull TherapyInfo therapyInfo) {
        for (JsonTherapy therapy : ckbJsonDatabase.therapies()) {
            if (therapy.id() == therapyInfo.id()) {
                return ImmutableTherapy.builder()
                        .id(therapy.id())
                        .createDate(therapy.createDate())
                        .updateDate(therapy.updateDate())
                        .therapyName(therapy.therapyName())
                        .drugs(DrugFactory.extractDrugs(ckbJsonDatabase, therapy.drugs()))
                        .synonyms(therapy.synonyms())
                        .description(ReferenceFactory.extractDescription("therapy", therapy.id(), therapy.descriptions()))
                        .references(ReferenceFactory.extractDescriptionReferences(ckbJsonDatabase, therapy.descriptions()))
                        .globalApprovalStatuses(convertGlobalApprovalStatuses(therapy.globalApprovalStatuses()))
                        .build();
            }
        }

        throw new IllegalStateException("Could not resolve CKB therapy with id '" + therapyInfo.id() + "'");
    }

    @NotNull
    private static List<GlobalApprovalStatus> convertGlobalApprovalStatuses(
            @NotNull List<GlobalApprovalStatusInfo> globalApprovalStatusesInfos) {
        List<GlobalApprovalStatus> globalApprovalStatuses = Lists.newArrayList();
        for (GlobalApprovalStatusInfo globalApprovalStatusInfo : globalApprovalStatusesInfos) {
            globalApprovalStatuses.add(ImmutableGlobalApprovalStatus.builder()
                    .id(globalApprovalStatusInfo.id())
                    .profileId(globalApprovalStatusInfo.molecularProfile().id())
                    .indicationId(globalApprovalStatusInfo.indication().id())
                    .approvalStatus(globalApprovalStatusInfo.approvalStatus())
                    .approvalAuthority(globalApprovalStatusInfo.approvalAuthority())
                    .build());
        }
        return globalApprovalStatuses;
    }
}