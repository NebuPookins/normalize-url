package net.nebupookins.normalizeurl;

import org.junit.Test;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UrlNormalizerTest {
    @Test
    public void testExampleCases() {
        final Function<String, String> normalizer = UrlNormalizer.semanticPreservingNormalizer();
        final String normalizedUrl = normalizer.apply("HTTP://Example.COM:80/bar/../%7Efoo%2a");
        assertThat(normalizedUrl, is("http://example.com/~foo%2A"));
    }

    private static void assertEquivalent(UrlNormalizer normalizer, String url1, String url2) {
        assertThat(normalizer.apply(url1), is(normalizer.apply(url2)));
    }

    private static void assertNotEquivalent(UrlNormalizer normalizer, String url1, String url2) {
        assertThat(normalizer.apply(url1), is(not(normalizer.apply(url2))));
    }

    private static void assertNormalizesPercentEncodedTripletsCase(UrlNormalizer underTest) {
        assertEquivalent(underTest, "http://example.com/foo%2a", "http://example.com/foo%2a");
        assertEquivalent(underTest, "http://example.com/foo%2a", "http://example.com/foo%2A");
        assertNotEquivalent(underTest, "http://example.com/foo%2b", "http://example.com/foo%2A");
        assertNotEquivalent(underTest, "http://example.com/foo%2aA", "http://example.com/foo%2aa");
        assertNotEquivalent(underTest, "http://example.com/foo%2aA", "http://example.com/foo%2Aa");
    }

    private static void assertNormalizesSchemeAndHost(UrlNormalizer underTest) {
        assertEquivalent(underTest, "HTTP://User@Example.COM/Foo", "http://User@example.com/Foo");
        assertNotEquivalent(underTest, "HTTP://User@Example.COM/Foo", "HTTP://USER@Example.COM/Foo");
        assertNotEquivalent(underTest, "HTTP://User@Example.COM/Foo", "HTTP://User@Example.COM/FOO");
    }

    private static void assertNormalizesPercentEncodedTripletOfUnreservedCharacters(UrlNormalizer underTest) {
        assertEquivalent(underTest, "http://example.com/%7Efoo", "http://example.com/~foo");
        assertEquivalent(underTest, "http://example.com/%7efoo", "http://example.com/~foo");
        assertNotEquivalent(underTest, "http://example.com/%7Efoo", "http://example.com/afoo");
        for (char i = 0; i < 0x100; i++) {
            final UrlOctet octet = new UrlOctet(i);
            if (!octet.needsEncoding()) {
                //There's 2 ways to represent the url: encoded or not encoded
                String encodedForm = "http://example.com/" + octet.asEncoded() + "foo";
                String literalForm = "http://example.com/" + octet.asChar() + "foo";
                assertEquivalent(underTest, encodedForm, literalForm);
            }
        }
    }

    private static void assertDoesNotTryToDecodedPercentTripletsIntoReservedCharacters(UrlNormalizer underTest) {
        for (char i = 0; i < 0x100; i++) {
            final UrlOctet octet = new UrlOctet(i);
            if (octet.needsEncoding()) {
                String alreadyFullyNormalizedPath = "/foo" + octet.asEncoded() + "bar";
                assertThat(underTest.apply("http://example.com" + alreadyFullyNormalizedPath), endsWith(alreadyFullyNormalizedPath));
            }
        }
    }

    private static void assertNormalizesDotSegments(UrlNormalizer underTest) {
        assertEquivalent(underTest, "http://example.com/foo/./bar/baz/../qux", "http://example.com/foo/bar/qux");
        assertNotEquivalent(underTest, "http://example.com/foo/./bar/../baz/../qux", "http://example.com/foo/bar/qux");
    }

    private static void assertNormalizesEmptyPath(UrlNormalizer underTest) {
        assertEquivalent(underTest, "http://example.com", "http://example.com/");
    }

    private static void assertNormalizesDefaultPort(UrlNormalizer underTest) {
        assertEquivalent(underTest, "http://example.com:80/", "http://example.com/");
        assertNotEquivalent(underTest, "http://example.com:81/", "http://example.com/");
    }

    @Test
    public void testSemanticPreservingNormalizer_NormalizesPercentEncodedTripletsCase() {
        assertNormalizesPercentEncodedTripletsCase(UrlNormalizer.semanticPreservingNormalizer());
    }

    @Test
    public void testSemanticPreservingNormalizer_NormalizesSchemeAndHost() {
        assertNormalizesSchemeAndHost(UrlNormalizer.semanticPreservingNormalizer());
    }

    @Test
    public void testSemanticPreservingNormalizer_NormalizesPercentEncodedTripletOfUnreservedCharacters() {
        assertNormalizesPercentEncodedTripletOfUnreservedCharacters(UrlNormalizer.semanticPreservingNormalizer());
    }

    @Test
    public void testSemanticPreservingNormalizer_NormalizesDotSegments() {
        assertNormalizesDotSegments(UrlNormalizer.semanticPreservingNormalizer());
    }

    @Test
    public void testSemanticPreservingNormalizer_NormalizesEmptyPath() {
        assertNormalizesEmptyPath(UrlNormalizer.semanticPreservingNormalizer());
    }

    @Test
    public void testSemanticPreservingNormalizer_NormalizesDefaultPort() {
        assertNormalizesDefaultPort(UrlNormalizer.semanticPreservingNormalizer());
    }

    @Test
    public void testSemanticPreservingNormalizer_DoesNotTryToDecodedPercentTripletsIntoReservedCharacters() {
        assertDoesNotTryToDecodedPercentTripletsIntoReservedCharacters(UrlNormalizer.semanticPreservingNormalizer());
    }
}
