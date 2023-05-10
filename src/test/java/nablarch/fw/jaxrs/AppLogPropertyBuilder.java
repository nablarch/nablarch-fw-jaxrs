package nablarch.fw.jaxrs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ログ出力プロパティ情報の作成補助。
 */
class AppLogPropertyBuilder {

    private final Map<String, String> props = new HashMap<String, String>();

    public AppLogPropertyBuilder beginOutputEnabled(String value) {
        props.put("jaxRsAccessLogFormatter.beginOutputEnabled", value);
        return this;
    }

    public AppLogPropertyBuilder endOutputEnabled(String value) {
        props.put("jaxRsAccessLogFormatter.endOutputEnabled", value);
        return this;
    }

    public AppLogPropertyBuilder beginFormat(String value) {
        props.put("jaxRsAccessLogFormatter.beginFormat", value);
        return this;
    }

    public AppLogPropertyBuilder endFormat(String value) {
        props.put("jaxRsAccessLogFormatter.endFormat", value);
        return this;
    }

    public AppLogPropertyBuilder parametersSeparator(String value) {
        props.put("jaxRsAccessLogFormatter.parametersSeparator", value);
        return this;
    }

    public AppLogPropertyBuilder sessionScopeSeparator(String value) {
        props.put("jaxRsAccessLogFormatter.sessionScopeSeparator", value);
        return this;
    }

    public AppLogPropertyBuilder datePattern(String value) {
        props.put("jaxRsAccessLogFormatter.datePattern", value);
        return this;
    }

    public AppLogPropertyBuilder maskingChar(String value) {
        props.put("jaxRsAccessLogFormatter.maskingChar", value);
        return this;
    }

    public AppLogPropertyBuilder maskingPatterns(String value) {
        props.put("jaxRsAccessLogFormatter.maskingPatterns", value);
        return this;
    }

    public AppLogPropertyBuilder bodyMaskingFilter(String value) {
        props.put("jaxRsAccessLogFormatter.bodyMaskingFilter", value);
        return this;
    }

    public AppLogPropertyBuilder bodyLogTargetMatcher(String value) {
        props.put("jaxRsAccessLogFormatter.bodyLogTargetMatcher", value);
        return this;
    }

    public AppLogPropertyBuilder maskingItemNames(String value) {
        props.put("jaxRsAccessLogFormatter.bodyMaskingItemNames", value);
        return this;
    }

    public AppLogPropertyBuilder beginTargets(String value) {
        props.put("jaxRsAccessLogFormatter.beginTargets", value);
        return this;
    }

    public AppLogPropertyBuilder endTargets(String value) {
        props.put("jaxRsAccessLogFormatter.endTargets", value);
        return this;
    }

    public AppLogPropertyBuilder messagePrefix(String value) {
        props.put("jaxRsAccessLogFormatter.structuredMessagePrefix", value);
        return this;
    }

    public AppLogPropertyBuilder beginLabel(String value) {
        props.put("jaxRsAccessLogFormatter.beginLabel", value);
        return this;
    }

    public AppLogPropertyBuilder endLabel(String value) {
        props.put("jaxRsAccessLogFormatter.endLabel", value);
        return this;
    }

    public Map<String, String> build() {
        return Collections.unmodifiableMap(props);
    }
}
