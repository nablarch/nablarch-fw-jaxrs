package nablarch.fw.jaxrs;

import nablarch.core.text.json.JsonSerializationSettings;
import nablarch.core.text.json.JsonSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * JSON文字列をシリアライズするクラス。
 */
public class JsonStringToJsonSerializer implements JsonSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JsonSerializationSettings settings) {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTarget(Class<?> valueClass) {
        return JsonString.class.isAssignableFrom(valueClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Writer writer, Object value) throws IOException {
        writer.append(((JsonString) value).value);
    }

    /**
     * JSON文字列を表現するクラス。
     */
    public static class JsonString {

        /** 値 */
        private final String value;

        /**
         * コンストラクタ
         *
         * @param value 値
         */
        public JsonString(String value) {
            this.value = value;
        }
    }
}
