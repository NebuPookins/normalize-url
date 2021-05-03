package net.nebupookins.normalizeurl;

import java.util.function.Function;

/**
 * <p>Tools to normalize a URL. From Wikipedia:</p>
 *
 * <blockquote>
 * <p>The goal of the normalization process is to transform a URI into a normalized URI so it is possible to determine
 * if two syntactically different URIs may be equivalent. [...] Web crawlers perform URI normalization in order to avoid
 * crawling the same resource more than once. Web browsers may perform normalization to determine if a link has been
 * visited or to determine if a page has been cached.</p>
 * </blockquote>
 *
 * @see <a href="https://en.wikipedia.org/wiki/URI_normalization">https://en.wikipedia.org/wiki/URI_normalization</a>
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-6">https://tools.ietf.org/html/rfc3986#section-6</a>
 */
public interface UrlNormalizer extends Function<String, String> {
    /**
     * Given a url, e.g. "HTTP://Example.COM:80/bar/../%7Efoo%2a", returns a normalized form of it, e.g.
     * "http://example.com/~foo%2A". The exact set of normalizations performed depend on the particular implementation
     * of UrlNormalizer.
     *
     * @param url the url to normalize; must be a valid non-null URL.
     * @return the normalized form of the url; will not be null.
     */
    @Override
    public String apply(String url);

    /**
     * @return a UrlNormalizer that performs "safe" normalization in the sense that all normalizations performed are the
     * type that all RFC-complying HTTP clients are expected to perform. Therefore such normalizations would be
     * invisible to any HTTP server, and so it would be impossible for the server to return content that differs
     * depending on which URL is used.
     */
    public static UrlNormalizer semanticPreservingNormalizer() {
        return new SemanticPreservingNormalizations();
    }
}

