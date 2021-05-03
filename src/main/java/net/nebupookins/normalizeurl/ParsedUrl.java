package net.nebupookins.normalizeurl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

class ParsedUrl {
    final String protocol;
    final Optional<String> userInfo;
    final Optional<String> host;
    final OptionalInt port;
    final String path; //Should contain the initial '/'
    final Optional<String> query;
    final Optional<String> ref;

    ParsedUrl(final String url) throws MalformedURLException {
        URL parsedUrl = new URL(url);
        this.protocol = parsedUrl.getProtocol();
        this.userInfo = Optional.ofNullable(parsedUrl.getUserInfo());
        this.host = Optional.ofNullable(parsedUrl.getHost());
        this.port = parsedUrl.getPort() == -1 ? OptionalInt.empty() : OptionalInt.of(parsedUrl.getPort());
        this.path = parsedUrl.getPath();
        this.query = Optional.ofNullable(parsedUrl.getQuery());
        this.ref = Optional.ofNullable(parsedUrl.getRef());
    }

    private ParsedUrl(
            String protocol,
            Optional<String> userInfo,
            Optional<String> host,
            OptionalInt port,
            String path,
            Optional<String> query,
            Optional<String> ref) {
        this.protocol = protocol;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.ref = ref;
    }

    ParsedUrl withProtocol(final String newProtocol) {
        if (protocol.equals(newProtocol)) {
            return this;
        } else {
            return new ParsedUrl(newProtocol, userInfo, host, port, path, query, ref);
        }
    }

    ParsedUrl withUserInfo(final Optional<String> newUserInfo) {
        if (userInfo.equals(newUserInfo)) {
            return this;
        } else {
            return new ParsedUrl(protocol, newUserInfo, host, port, path, query, ref);
        }
    }

    ParsedUrl withHost(final Optional<String> newHost) {
        if (host.equals(newHost)) {
            return this;
        } else {
            return new ParsedUrl(protocol, userInfo, newHost, port, path, query, ref);
        }
    }

    ParsedUrl withPort(final OptionalInt newPort) {
        if (port.equals(newPort)) {
            return this;
        } else {
            return new ParsedUrl(protocol, userInfo, host, newPort, path, query, ref);
        }
    }

    ParsedUrl withPath(final String newPath) {
        if (path.equals(newPath)) {
            return this;
        } else {
            return new ParsedUrl(protocol, userInfo, host, port, newPath, query, ref);
        }
    }

    ParsedUrl withQuery(final Optional<String> newQuery) {
        if (query.equals(newQuery)) {
            return this;
        } else {
            return new ParsedUrl(protocol, userInfo, host, port, path, newQuery, ref);
        }
    }

    ParsedUrl withRef(final Optional<String> newRef) {
        if (ref.equals(newRef)) {
            return this;
        } else {
            return new ParsedUrl(protocol, userInfo, host, port, path, query, newRef);
        }
    }

    private static <I, O> Function<Optional<I>, Optional<O>> liftIntoOptional(Function<I, O> f) {
        return i -> i.map(f);
    }

    ParsedUrl transformFields(Function<String, String> transform) {
        final Function<Optional<String>, Optional<String>> lifted = liftIntoOptional(transform);
        return this
                .withProtocol(transform.apply(this.protocol))
                .withUserInfo(lifted.apply(this.userInfo))
                .withHost(lifted.apply(this.host))
                .withPath(transform.apply(this.path))
                .withQuery(lifted.apply(this.query))
                .withRef(lifted.apply(this.ref));
    }

    @Override
    public String toString() {
        final StringBuilder retVal = new StringBuilder();
        retVal.append(protocol);
        retVal.append(':');
        host.ifPresent(host -> {
            retVal.append("//");
            userInfo.ifPresent(user -> {
                retVal.append(user);
                retVal.append('@');
            });
            retVal.append(host);
            port.ifPresent(port -> {
                retVal.append(':');
                retVal.append(port);
            });
        });
        retVal.append(path);
        query.ifPresent(query -> {
            retVal.append('?');
            retVal.append(query);
        });
        ref.ifPresent(ref -> {
            retVal.append('#');
            retVal.append(ref);
        });
        return retVal.toString();
    }
}
