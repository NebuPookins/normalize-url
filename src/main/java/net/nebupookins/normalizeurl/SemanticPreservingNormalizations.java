package net.nebupookins.normalizeurl;

import java.net.MalformedURLException;
import java.util.*;

class SemanticPreservingNormalizations implements UrlNormalizer {

    /**
     * <blockquote>For all URIs, the hexadecimal digits within a percent-encoding
     * triplet (e.g., "%3a" versus "%3A") are case-insensitive and therefore
     * should be normalized to use uppercase letters for the digits A-F.
     *
     * <cite>https://tools.ietf.org/html/rfc3986#section-6.2.2.1</cite>
     * </blockquote>
     */
    static String convertPercentEncodedTripletsToUpperCase(String url) {
        final int EXPECTING_ANY = 0;
        final int EXPECTING_ENCODED_1 = EXPECTING_ANY + 1;
        final int EXPECTING_ENCODED_2 = EXPECTING_ENCODED_1 + 1;

        final StringBuilder retVal = new StringBuilder(url.length());
        int state = EXPECTING_ANY;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            switch (state) {
                case EXPECTING_ANY:
                    retVal.append(c);
                    if (c == '%') {
                        state = EXPECTING_ENCODED_1;
                    }
                    break;
                case EXPECTING_ENCODED_1:
                    retVal.append(Character.toUpperCase(c));
                    state = EXPECTING_ENCODED_2;
                    break;
                case EXPECTING_ENCODED_2:
                    retVal.append(Character.toUpperCase(c));
                    state = EXPECTING_ANY;
                    break;
                default:
                    throw new RuntimeException("Bug in code, should never get here.");
            }
        }
        if (state != EXPECTING_ANY) {
            throw new RuntimeException("Unexpected end of string for url " + url);
        }
        return retVal.toString();
    }

    /**
     * <blockquote>When a URI uses components of the generic syntax, the component
     * syntax equivalence rules always apply; namely, that the scheme and
     * host are case-insensitive and therefore should be normalized to
     * lowercase.  For example, the URI "HTTP://www.EXAMPLE.com/" is
     * equivalent to "http://www.example.com/".
     *
     * <cite>https://tools.ietf.org/html/rfc3986#section-6.2.2.1</cite>
     * </blockquote>
     */
    static ParsedUrl convertSchemeAndHostToLowercase(ParsedUrl url) {
        return url
                .withProtocol(url.protocol.toLowerCase())
                .withHost(url.host.map(String::toLowerCase));
    }

    /**
     * <blockquote>some URI producers percent-encode
     * octets that do not require percent-encoding, resulting in URIs that
     * are equivalent to their non-encoded counterparts.  These URIs should
     * be normalized by decoding any percent-encoded octet that corresponds
     * to an unreserved character,
     *
     * <cite>https://tools.ietf.org/html/rfc3986#section-6.2.2.2</cite>
     * </blockquote>
     */
    static String decodePercentEncodedTripletsOfUnreservedCharacters(String url) {
        final int EXPECTING_ANY = 0;
        final int EXPECTING_ENCODED_1 = EXPECTING_ANY + 1;
        final int EXPECTING_ENCODED_2 = EXPECTING_ENCODED_1 + 1;

        final StringBuilder retVal = new StringBuilder(url.length());
        final StringBuilder currentTriplet = new StringBuilder(3);
        int state = EXPECTING_ANY;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            switch (state) {
                case EXPECTING_ANY:
                    if (c == '%') {
                        state = EXPECTING_ENCODED_1;
                    } else {
                        retVal.append(c);
                    }
                    break;
                case EXPECTING_ENCODED_1:
                    currentTriplet.append(c);
                    state = EXPECTING_ENCODED_2;
                    break;
                case EXPECTING_ENCODED_2:
                    currentTriplet.append(c);
                    char asciiCode = (char)Integer.parseInt(currentTriplet.toString(), 16);
                    retVal.append(new UrlOctet(asciiCode).toString());
                    currentTriplet.setLength(0);
                    state = EXPECTING_ANY;
                    break;
                default:
                    throw new RuntimeException("Bug in code, should never get here.");
            }
        }
        if (state != EXPECTING_ANY) {
            throw new RuntimeException("Unexpected end of string for url " + url);
        }
        return retVal.toString();
    }

    static String normalizePathDotSegments(String path) {
        final Queue<String> input = new ArrayDeque<>();
        Collections.addAll(input, path.split("(?=/)"));
        final Deque<String> output = new ArrayDeque<>();
        while (!input.isEmpty()) {
            final String segment = input.remove();
            switch (segment) {
                case "/.":
                    //ignore it
                    break;
                case "/..":
                    output.poll();
                    break;
                default:
                    output.push(segment);
                    break;
            }
        }
        return String.join("", output);
    }

    static ParsedUrl normalizeEmptyPath(ParsedUrl url) {
        if (url.path.isEmpty()) {
            return url.withPath("/");
        } else {
            return url;
        }
    }

    static ParsedUrl normalizeDefaultPort(ParsedUrl url) {
        if (!url.port.isPresent()) {
            return url;
        }
        int port = url.port.getAsInt();
        if (port == 80) {
            return url.withPort(OptionalInt.empty());
        } else {
            return url;
        }
    }

    @Override
    public String apply(String url) {
        ParsedUrl parsedUrl;
        try {
            parsedUrl = new ParsedUrl(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        parsedUrl = parsedUrl.transformFields(SemanticPreservingNormalizations::convertPercentEncodedTripletsToUpperCase);
        parsedUrl = convertSchemeAndHostToLowercase(parsedUrl);
        parsedUrl = parsedUrl.transformFields(SemanticPreservingNormalizations::decodePercentEncodedTripletsOfUnreservedCharacters);
        parsedUrl = parsedUrl.withPath(normalizePathDotSegments(parsedUrl.path));
        parsedUrl = normalizeEmptyPath(parsedUrl);
        parsedUrl = normalizeDefaultPort(parsedUrl);
        return parsedUrl.toString();
    }
}
