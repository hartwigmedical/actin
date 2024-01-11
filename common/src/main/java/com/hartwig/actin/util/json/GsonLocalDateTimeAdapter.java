package com.hartwig.actin.util.json;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GsonLocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(@NotNull JsonWriter writer, @Nullable LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            writer.nullValue();
        } else {
            writer.value(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

    @Override
    @Nullable
    public LocalDateTime read(@NotNull JsonReader reader) throws IOException {
        JsonToken firstToken = reader.peek();
        if (firstToken == JsonToken.NULL) {
            return null;
        }

        String dateTimeString = reader.nextString();
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
    }
}
