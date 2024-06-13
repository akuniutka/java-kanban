package io.github.akuniutka.kanban.web;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.akuniutka.kanban.exception.ManagerValidationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(localDateTime.toString());
        }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        JsonToken type = jsonReader.peek();
        if (type == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        try {
            return LocalDateTime.parse(jsonReader.nextString());
        } catch (IllegalStateException | DateTimeParseException exception) {
            throw new ManagerValidationException("wrong value for date and time");
        }
    }
}
