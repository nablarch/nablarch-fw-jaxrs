package nablarch.fw.jaxrs;

import nablarch.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ログ出力するJAX-RSのボディ文字列をマスク処理するフィルタ。
 */
public class JaxRsBodyMaskingFilter implements LogContentMaskingFilter {

    /** プロパティ名のプレフィックス */
    private static final String PROP_PREFIX = "jaxRsAccessLogFormatter.";

    /** マスク文字 */
    private String maskingString;

    /** マスク対象のJSON文字列パターン */
    private List<Pattern> maskingJsonPatterns;

    @Override
    public void initialize(Map<String, String> props) {
        maskingString = getMaskingString(props);
        maskingJsonPatterns = getMaskingJsonPatterns(props);
    }

    /**
     * マスク文字列を取得する。
     *
     * @param props 各種ログ出力の設定情報
     * @return マスク文字列
     */
    protected String getMaskingString(Map<String, String> props) {
        char maskingChar = getMaskingChar(props);
        return StringUtil.repeat(maskingChar, 5);
    }

    /**
     * マスク文字を取得する。
     *
     * @param props 各種ログ出力の設定情報
     * @return マスク文字
     */
    protected char getMaskingChar(Map<String, String> props) {
        String value = props.get(PROP_PREFIX + "maskingChar");
        if (value == null) {
            return '*';
        }
        if (value.length() > 1) {{
            throw new IllegalArgumentException(
                    String.format("maskingChar was not char type. maskingChar = [%s]", value));
        }}
        return value.charAt(0);
    }

    /**
     * マスク対象のJSON文字列パターンを取得する。
     *
     * @param props 各種ログ出力の設定情報
     * @return マスク対象のパターン
     */
    protected List<Pattern> getMaskingJsonPatterns(Map<String, String> props) {
        String value = props.get(PROP_PREFIX + "bodyMaskingItemNames");
        if (value == null || value.matches("^[ ,]*$")) {
            return Collections.emptyList();
        }
        List<Pattern> patterns = new ArrayList<Pattern>();
        // 部分一致や完全一致を指定できるように正規表現を指定可能にすることも検討したが、
        // 複雑なパターンを指定された際にJSON文字列にマッチするように変換するのが難しい。
        // また、正規表現の先読み後読みを使用するため特定の構文を使用できないこともあり、
        // JSONの項目名のみを指定してもらう方針とする
        String itemNameRegex = "\"(?:" + value.replace(",", "|") + ")\"";
        String stringValueRegex = "(?:(?!(?<!\\\\)\").)*";
        String numberValueRegex = "[+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?";
        String booleanValueRegex = "true|false";
        // 1つのPatternにまとめたかったが、二重引用符の有無を考慮しつつ後読みで
        // マッチさせる正規表現が不明だったため、二重引用符の有無で分けている
        String quotedItemRegex = "(?<=" + itemNameRegex + ":(?: |)\")" + stringValueRegex + "(?=\")";
        String nonQuotedItemRegex = "(?<=" + itemNameRegex + ":(?: |))(" + numberValueRegex + "|" + booleanValueRegex + ")";
        patterns.add(Pattern.compile(quotedItemRegex, Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile(nonQuotedItemRegex, Pattern.CASE_INSENSITIVE));
        return patterns;
    }

    @Override
    public String mask(String content) {
        if (content.isEmpty()) {
            return content;
        }
        // JAX-RSのボディ形式としてJSONとXMLを扱っているが、現時点ではJSONのみ対応する
        if (content.startsWith("<?xml")) {
            return content;
        }
        List<Pattern> maskingPatterns = maskingJsonPatterns;

        String result = content;
        for (Pattern maskingPattern : maskingPatterns) {
            // Matcher#appendReplacementがStringBuilderに対応していないため、StringBufferを使う
            StringBuffer buf = new StringBuffer();
            Matcher matcher = maskingPattern.matcher(result);
            while(matcher.find()) {
                matcher.appendReplacement(buf, maskingString);
            }
            matcher.appendTail(buf);
            result = buf.toString();
        }
        return result;
    }
}
