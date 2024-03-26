package com.promcteam.codex.api;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * Class for replacing strings from a template to a supplied value
 */
public class Replacer {
    @Getter
    private final String           from;
    private final Supplier<String> to;

    public Replacer(final String from, final String to) {
        this.from = (from == null) ? "null" : from;
        this.to = (to == null) ? () -> "null" : () -> to;
    }

    public Replacer(final String from, final Supplier<String> to) {
        this.from = (from == null) ? "null" : from;
        this.to = (to == null) ? () -> "null" : to;
    }

    public Replacer(final String from, final Object to) {
        this.from = (from == null) ? "null" : from;
        this.to = (to == null) ? () -> "null" : to::toString;
    }

    public static Replacer replacer(final String from, final Supplier<String> to) {
        return new Replacer((from == null) ? "null" : from, to);
    }

    public static Replacer replacer(final String from, final String to) {
        return new Replacer((from == null) ? "null" : from, (to == null) ? "null" : to);
    }

    public static Replacer replacer(final String from, final Object to) {
        return new Replacer((from == null) ? "null" : from, (to == null) ? "null" : to.toString());
    }

    public String getTo() {
        return this.to.get();
    }

    public Supplier<String> getToSupplier() {
        return this.to;
    }

    public String use(final String str) {
        return StringUtils.replace(str, this.from, this.to.get());
    }

    public static String use(String str, Replacer... r) {
        for (final Replacer r1 : r) {
            str = r1.use(str);
        }
        return str;
    }

    @Override
    public String toString() {
        return "Replacer{" + "from='" + this.from + '\'' + ", to='" + this.to + '\'' + '}';
    }
}
