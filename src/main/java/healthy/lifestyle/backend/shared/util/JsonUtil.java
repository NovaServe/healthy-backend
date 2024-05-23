package healthy.lifestyle.backend.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonUtil {
    @Autowired
    ObjectMapper objectMapper;

    public List<JsonDescription> deserializeJsonStringToJsonDescriptionList(String jsonString)
            throws JsonProcessingException {
        TypeReference<List<JsonDescription>> typeReference = new TypeReference<List<JsonDescription>>() {};
        return objectMapper.readValue(jsonString, typeReference);
    }

    public List<JsonDescription> processJsonDescription(List<JsonDescription> jsonDescriptionList) {
        // Convert user timezone to db timezone

        // Add json_ids

        return null;
    }

    public String serializeJsonDescriptionList(List<JsonDescription> jsonDescriptionList) throws JsonProcessingException {
       return objectMapper.writeValueAsString(jsonDescriptionList);
    }
}
