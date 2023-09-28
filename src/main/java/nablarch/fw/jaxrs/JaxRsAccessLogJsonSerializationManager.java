package nablarch.fw.jaxrs;

import nablarch.core.log.basic.AppLogMapToJsonSerializer;
import nablarch.core.text.json.ArrayToJsonSerializer;
import nablarch.core.text.json.BasicJsonSerializationManager;
import nablarch.core.text.json.BooleanToJsonSerializer;
import nablarch.core.text.json.CalendarToJsonSerializer;
import nablarch.core.text.json.DateToJsonSerializer;
import nablarch.core.text.json.JsonSerializationManager;
import nablarch.core.text.json.JsonSerializationSettings;
import nablarch.core.text.json.JsonSerializer;
import nablarch.core.text.json.ListToJsonSerializer;
import nablarch.core.text.json.LocalDateTimeToJsonSerializer;
import nablarch.core.text.json.NumberToJsonSerializer;
import nablarch.core.text.json.StringToJsonSerializer;
import nablarch.core.util.annotation.Published;

import java.util.Arrays;
import java.util.List;

/**
 * RESTfulウェブサービスのアクセスログのJSON形式による出力に対応した{@link JsonSerializationManager}の実装クラス。
 */
@Published(tag = "architect")
public class JaxRsAccessLogJsonSerializationManager extends BasicJsonSerializationManager {

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<JsonSerializer> createSerializers(JsonSerializationSettings settings) {
        // リクエストボディがJSON文字列の場合、BasicJsonSerializationManagerだと
        // 文字列とみなして二重引用符で囲われるため、多くの箇所がエスケープされてしまう。
        // 可読性を考慮し、JSON文字列は値としてそのまま出力するように設定しておく。
        return Arrays.asList(
                new JsonStringToJsonSerializer(),
                new StringToJsonSerializer(),
                new DateToJsonSerializer(this),
                new AppLogMapToJsonSerializer(this),
                new ListToJsonSerializer(this),
                new ArrayToJsonSerializer(this),
                new NumberToJsonSerializer(this),
                new BooleanToJsonSerializer(),
                new CalendarToJsonSerializer(this),
                new LocalDateTimeToJsonSerializer(this));
    }
}
