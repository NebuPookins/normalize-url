package net.nebupookins.normalizeurl;

/**
 * Represents a single "character" in a URL, after byte decoding. That is, "a" is a single character in an URL, but "%20"
 * is also a single "character" in an URL.
 */
public class UrlOctet {
    private final char octet;

    public UrlOctet(char octet) {
        if (octet > 0xFF) {
            throw new IllegalArgumentException("Url char must be between 0x00 and 0xFF");
        }
        this.octet = octet;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof UrlOctet) {
            return equals((UrlOctet) that);
        } else {
            return false;
        }
    }

    public boolean equals(UrlOctet that) {
        return this.octet == that.octet;
    }

    @Override
    public int hashCode() {
        return octet;
    }

    private boolean isAlpha() {
        return 0x41 <= octet && octet <= 0x5A;
    }

    private boolean isDigit() {
        return 0x30 <= octet && octet <= 0x39;
    }

    private boolean isHyphen() {
        return octet == 0x2D;
    }

    private boolean isPeriod() {
        return octet == 0x2E;
    }

    private boolean isUnderscore() {
        return octet == 0x5F;
    }

    private boolean isTilde() {
        return octet == 0x7E;
    }

    /**
     * @return true if this character is a reserved character and must be encoded when present in a URL; false if it can
     * be represented literally.
     */
    public boolean needsEncoding() {
        return !(isAlpha() || isDigit() || isHyphen() || isPeriod() || isUnderscore() || isTilde());
    }

    /**
     * @return the encoded form of this character, e.g. "%20", regardless of whether encoding would be needed for this
     * character..
     */
    public String asEncoded() {
        String hexString = Integer.toString(octet, 16).toUpperCase();
        switch (hexString.length()) {
            case 1:
                return "%0" + hexString;
            case 2:
                return "%" + hexString;
            default:
                throw new RuntimeException("Bug in code; this should never happen. Got hexString: " + hexString);
        }
    }

    /**
     * @return the decoded form of this character, e.g. ' ', regardless of whether encoding would be needed for this
     * character..
     */
    public char asChar() {
        return octet;
    }

    /**
     * @return how this character should appear when in an percent encoded URL, e.g. "a" or "%20".
     */
    @Override
    public String toString() {
        if (needsEncoding()) {
            return asEncoded();
        } else {
            return Character.toString(octet);
        }
    }
}
