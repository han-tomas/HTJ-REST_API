package htj.hantomas.htjrestapi.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;


import java.io.IOException;
@JsonComponent
public class ErrorsSerializer extends JsonSerializer<Errors> {

    @Override
    public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray(); // errors안에는 에러가 여러개이기 때문에 배열로 담아주기 위해 사용
        errors.getFieldErrors().stream().forEach(e -> {
            try {
                gen.writeStartObject();

                gen.writeStringField("field",e.getField());
                gen.writeStringField("objectName",e.getObjectName());
                gen.writeStringField("code",e.getCode());
                gen.writeStringField("defaultMessage",e.getDefaultMessage());
                Object rejectedValue = e.getRejectedValue();
                if (rejectedValue != null){
                    gen.writeStringField("rejectedValue",rejectedValue.toString());
                }

                gen.writeEndObject();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });
        errors.getGlobalErrors().forEach(e -> {
            try {
                gen.writeStartObject();
                // GloblaErrors의 경우 Field가 없다.
                //gen.writeStringField("field",e.getField());
                gen.writeStringField("objectName",e.getObjectName());
                gen.writeStringField("code",e.getCode());
                gen.writeStringField("defaultMessage",e.getDefaultMessage());
                /*
                    Object rejectedValue = e.getRejectedValue();
                    if (rejectedValue != null){
                        gen.writeStringField("rejectedValue",rejectedValue.toString());
                    }
                */

                gen.writeEndObject();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        gen.writeEndArray();
    }
}
