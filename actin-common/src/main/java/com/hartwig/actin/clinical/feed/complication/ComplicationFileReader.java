package com.hartwig.actin.clinical.feed.complication;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedFileReader;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class ComplicationFileReader extends FeedFileReader<ComplicationEntry> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    public ComplicationFileReader() {
    }

    @NotNull
    @Override
    public ComplicationEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableComplicationEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .identifierSystem(parts[fieldIndexMap.get("identifier_system")])
                .categoryCodeOriginal(parts[fieldIndexMap.get("category_code_original")])
                .categoryDisplay(parts[fieldIndexMap.get("category_display")])
                .categoryDisplayOriginal(parts[fieldIndexMap.get("category_display_original")])
                .clinicalStatus(parts[fieldIndexMap.get("clinicalStatus")])
                .codeCodeOriginal(parts[fieldIndexMap.get("code_code_original")])
                .codeDisplayOriginal(parts[fieldIndexMap.get("code_display_original")])
                .codeCode(parts[fieldIndexMap.get("code_code")])
                .codeDisplay(parts[fieldIndexMap.get("code_display")])
                .onsetPeriodStart(FeedUtil.parseDate(parts[fieldIndexMap.get("onsetPeriod_start")], FORMAT))
                .onsetPeriodEnd(FeedUtil.parseOptionalDate(parts[fieldIndexMap.get("onsetPeriod_end")], FORMAT))
                .severityCode(parts[fieldIndexMap.get("severity_code")])
                .severityDisplay(parts[fieldIndexMap.get("severity_display")])
                .severityDisplayNl(parts[fieldIndexMap.get("severity_display_nl")])
                .specialtyCodeOriginal(parts[fieldIndexMap.get("specialty_code_original")])
                .specialtyDisplayOriginal(parts[fieldIndexMap.get("specialty_display_original")])
                .verificationStatusCode(parts[fieldIndexMap.get("verificationStatus_code")])
                .build();
    }
}
