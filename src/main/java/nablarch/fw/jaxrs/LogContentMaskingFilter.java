package nablarch.fw.jaxrs;

import nablarch.core.util.annotation.Published;

import java.util.Map;

/**
 * ログに出力する文字列をマスク処理するためのフィルタ。
 */
@Published(tag = "architect")
public interface LogContentMaskingFilter {

    /**
     * 初期化する。
     *
     * @param props 各種ログ出力の設定情報
     */
    void initialize(Map<String, String> props);

    /**
     * マスク対象のパターンにマッチする箇所をマスクする。
     *
     * @param content マスク対象の文字列
     * @return マスク後の文字列
     */
    String mask(String content);
}
