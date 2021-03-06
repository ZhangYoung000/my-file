package cn.zyity.zfile.model.enums;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 */
public class StorageTypeEnumJsonDeSerializerConvert extends JsonDeserializer<StorageTypeEnum> {

    @Override
    public StorageTypeEnum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return StorageTypeEnum.getEnum(jsonParser.getText());
    }
}