package pt.wastemanagement.api.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import pt.wastemanagement.api.views.output.json_home.JsonHomeOutput;


import java.io.IOException;
import java.lang.reflect.Type;

public class JsonHomeMessageConverter extends AbstractGenericHttpMessageConverter<JsonHomeOutput> {

    public JsonHomeMessageConverter(){
        super(new MediaType("application","json-home"));
    }

    @Override
    protected void writeInternal(JsonHomeOutput jsonHomeOutput, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputMessage.getBody(),jsonHomeOutput);
    }

    @Override
    protected JsonHomeOutput readInternal(Class<? extends JsonHomeOutput> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    public JsonHomeOutput read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }
}
