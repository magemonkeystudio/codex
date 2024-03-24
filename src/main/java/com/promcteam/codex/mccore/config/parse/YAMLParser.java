/**
 * MCCore
 * com.rit.sucy.config.parse.YAMLParser
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2024 ProMCTeam
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.promcteam.codex.mccore.config.parse;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Custom parser for YAML that preserves comments with
 * the key they precede.
 *
 * Fixed up Feb 2024 by Travja for more actual YAML spec compliance.
 */
public class YAMLParser {
    private static final Pattern      LIST_PATTERN     = Pattern.compile(" *- .+");
    private static final Pattern      MULTILINE_MARKER = Pattern.compile(".+: ([|>]|['\"]?.+).*");
    private              List<String> comments         = new ArrayList<>();
    private              int          i                = 0;

    /**
     * Reads and then parses data from an embedded plugin resource. If
     * the resource does not exist or doesn't contain any data, this
     * will return an empty DataSection object.
     *
     * @param plugin plugin containing the embedded resource
     * @param path   path to the resource (not including the beginning slash)
     * @return loaded data
     */
    public DataSection parseResource(Plugin plugin, String path) {
        try (InputStream read = plugin.getClass().getResourceAsStream("/" + path)) {
            StringBuilder builder = new StringBuilder();
            byte[]        data    = new byte[1024];
            int           bytes;
            do {
                bytes = read.read(data);
                builder.append(new String(data, 0, bytes, StandardCharsets.UTF_8));
            } while (bytes == 1024);
            return parseText(builder.toString());
        } catch (Exception ex) {
            // Do nothing
            ex.printStackTrace();
            Bukkit.getLogger().info("Failed to parse resource (" + path + ") - " + ex.getMessage());
        }
        return new DataSection();
    }

    /**
     * Reads and then parses data from the file at the given path. If
     * the file does not exist or doesn't contain any data, this
     * will return an empty DataSection object.
     *
     * @param path path to the file load from
     * @return loaded data
     */
    public DataSection parseFile(String path) {
        return parseFile(new File(path));
    }

    /**
     * Reads and then parses data from the file. If
     * the file does not exist or doesn't contain any data, this
     * will return an empty DataSection object.
     *
     * @param file the file load from
     * @return loaded data
     */
    public DataSection parseFile(File file) {
        try {
            if (file.exists()) {
                FileInputStream read = new FileInputStream(file);
                byte[]          data = new byte[(int) file.length()];
                read.read(data);
                read.close();
                return parseText(new String(data, StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            // Do nothing
            ex.printStackTrace();
        }
        return new DataSection();
    }

    /**
     * Parses the text read in from a file. If a null string
     * is passed in, this will return an empty data section.
     *
     * @param text text to parse
     * @return parsed data
     */
    public DataSection parseText(String text) {
        return parseText(text, '\'');
    }

    /**
     * Parses the text read in from a file. If a null string
     * is passed in, this will return an empty data section.
     *
     * @param text  text to parse
     * @param quote character strings are wrapped in
     * @return parsed data
     */
    public DataSection parseText(String text, char quote) {
        if (text == null) return new DataSection();
        text = text.replaceAll("\r\n", "\n").replaceAll("\n *\n", "\n").replaceAll(" +\n", "\n");
        String[] lines = text.split("\n");
        i = 0;
        return parse(lines, 0, quote);
    }

    /**
     * Parses YAML data into DataSection objects
     *
     * @param lines  lines to parse
     * @param indent current indent
     * @param quote  character strings are wrapped in
     * @return parsed data
     */
    private DataSection parse(String[] lines, int indent, char quote) {
        DataSection data = new DataSection();
        int         spaces;
        while (i < lines.length && ((spaces = countSpaces(lines[i])) >= indent || lines[i].length() == 0
                || lines[i].charAt(spaces) == '#')) {
            String entry = lines[i];

            // When the entire line is just spaces, continue
            if (entry.trim().isEmpty()) {
                i++;
                continue;
            }

            // Comments
            if (entry.charAt(spaces) == '#') {
                comments.add(entry.substring(spaces + 1));
                i++;
                continue;
            }

            while (i < lines.length && (spaces != indent)) {
                i++;
            }
            if (i == lines.length) return data;

            try {
                String key = entry.substring(indent, entry.indexOf(':'));
                if ((key.charAt(0) == '\'' && key.charAt(key.length() - 1) == '\'') || (key.charAt(0) == '"'
                        && key.charAt(key.length() - 1) == '"')) {
                    key = key.substring(1, key.length() - 1);
                }
                data.setComments(key, comments);
                comments.clear();

                // New empty section
                if (entry.indexOf(": {}") == entry.length() - 4 && entry.length() >= 4) {
                    data.createSection(key);
                }

                // String list
                else if (i < lines.length - 1 && lines[i + 1].length() > indent + 1
                        && LIST_PATTERN.matcher(lines[i + 1].substring(indent)).matches()
                        && (countSpaces(lines[i + 1]) == indent
                        || countSpaces(lines[i + 1]) == indent + 2)) {
                    int               listIndent = countSpaces(lines[i + 1]);
                    ArrayList<String> stringList = new ArrayList<>();
                    while (++i < lines.length && lines[i].length() > listIndent
                            && LIST_PATTERN.matcher(lines[i].substring(listIndent)).matches()) {
                        if (i + 1 < lines.length && countSpaces(lines[i + 1]) > listIndent) {
                            stringList.add(buildMultiline(lines, listIndent, quote, lines[i]));
                            continue;
                        }

                        String str = lines[i].substring(listIndent + 2);
                        if (str.length() > 0 && str.charAt(0) == quote)
                            while (str.length() > 0 && str.charAt(0) == quote) str = str.substring(1, str.length() - 1);
                        else if (str.length() > 0 && str.charAt(0) == '"')
                            while (str.length() > 0 && str.charAt(0) == '"') str = str.substring(1, str.length() - 1);
                        else if (str.length() > 0 && str.charAt(0) == '\'')
                            while (str.length() > 0 && str.charAt(0) == '\'') str = str.substring(1, str.length() - 1);

                        str = str.replace("\\'", "'").replace("\\\"", "\"");
                        stringList.add(str);
                    }
                    data.set(key, stringList);
                    i--;
                }

                // List, with one-line syntax
                else if (entry.substring(indent).startsWith(key + ": [") && entry.endsWith("]")) {
                    String       value = entry.substring(entry.indexOf('[') + 1, entry.lastIndexOf(']'));
                    String[]     parts = value.split(", *");
                    List<String> list  = new ArrayList<>();

                    for (String part : parts) {
                        if (part.startsWith("'") || part.startsWith("\"")) {
                            part = part.substring(1, part.length() - 1);
                        }
                        part = part.replace("\\'", "'").replace("\\\"", "\"");
                        if (!part.isBlank()) list.add(part);
                    }
                    data.set(key, list);
                }

                // New section with content OR multiline string
                else if (i < lines.length - 1 && countSpaces(lines[i + 1]) > indent) {
                    if (MULTILINE_MARKER.matcher(entry).matches() && StringUtils.isNotBlank(lines[i + 1])) {
                        data.set(key, buildMultiline(lines, indent, quote, entry));
                        i++;
                        continue;
                    }

                    i++;
                    int         newIndent = countSpaces(lines[i]);
                    DataSection node      = parse(lines, newIndent, quote);
                    data.set(key, node);
                    continue;
                }

                // New empty section
                else if (entry.indexOf(':') == entry.length() - 1) {
                    data.set(key, new DataSection());
                }

                // Regular value
                else {
                    String str = entry.substring(entry.indexOf(':') + 2);
                    Object value;
                    if (str.charAt(0) == quote) value = str.substring(1, str.length() - 1);
                    else if (str.charAt(0) == '\'') value = str.substring(1, str.length() - 1);
                    else if (str.charAt(0) == '"') value = str.substring(1, str.length() - 1);
                    else value = str;
                    value = value.toString().replace("\\'", "'").replace("\\\"", "\"");

                    if (value.equals("[]")) value = new ArrayList<>();
                    data.set(key, value);
                }
            } catch (Exception e) {
                if (e instanceof YAMLException)
                    throw (YAMLException) e; // Let it bubble up

                throw new YAMLException(
                        "There was a problem parsing the YAML file at line " + (i + 1) + " \"" + entry + "\": "
                                + e.getMessage(), e);
            }

            i++;
        }
        return data;
    }

    private String buildMultiline(String[] lines, int indent, char quote, String entry) {
        StringBuilder multiLine = new StringBuilder();
        String        str;
        if (LIST_PATTERN.matcher(entry).matches()) str = entry.substring(entry.indexOf('-') + 2);
        else if (entry.contains(":")) str = entry.substring(entry.indexOf(':') + 2);
        else str = entry.substring(indent + 2);

        boolean piped  = false;
        boolean folded = false;

        if (str.length() > 0 && str.charAt(0) == '|') piped = true;
        else if (str.length() > 0 && str.charAt(0) == '>') folded = true;

        if (piped || folded) {
            while (i + 1 < lines.length && countSpaces(lines[i + 1]) > indent) {
                String line = lines[++i].substring(indent + 2);
                if (line.isBlank()) {
                    multiLine.append('\n');
                    continue;
                }
                if (multiLine.length() > 0) multiLine.append(piped ? '\n' : ' ');
                multiLine.append(line);

                boolean inList = i + 1 < lines.length && countSpaces(lines[i + 1]) > indent;
                if (!inList) break;
            }
            return multiLine.toString();
        } else {
            String quoteChar = "";
            if (str.length() > 0 && str.charAt(0) == quote) quoteChar = String.valueOf(quote);
            else if (str.length() > 0 && str.charAt(0) == '\'') quoteChar = "\'";
            else if (str.length() > 0 && str.charAt(0) == '"') quoteChar = "\"";

            if (quoteChar.isEmpty()) {
                multiLine.append(str);
            } else {
                if (!quoteChar.isBlank() && str.endsWith(quoteChar)) {
                    multiLine.append(str.substring(1, str.length() - 1));
                    return multiLine.toString();
                }
                multiLine.append(str.substring(quoteChar.length()));
            }

            // Iterate over the lines until we find an ending quote
            int spaces;
            while (i + 1 < lines.length && (spaces = countSpaces(lines[i + 1])) > indent) {
                String line = lines[++i];
                if (line.isBlank()) {
                    multiLine.append('\n');
                    continue;
                }

                if (!quoteChar.isBlank() && line.endsWith(quoteChar)) {
                    multiLine.append(" ").append(line.substring(spaces, line.length() - 1));
                    break;
                }
                multiLine.append(" ").append(line.substring(spaces));

                boolean inList = i + 1 < lines.length && countSpaces(lines[i + 1]) > indent;
                if (!inList) break;
            }
        }
        return multiLine.toString();
    }

    /**
     * Counts the number of leading spaces in the string
     *
     * @param line line to count the leading spaces for
     * @return the number of leading spaces
     */
    private int countSpaces(String line) {
        return IntStream.range(0, line.length()).filter(i -> line.charAt(i) != ' ').findFirst().orElse(0);
    }

    /**
     * Saves config data to a file
     *
     * @param path path to the file
     */
    public void save(DataSection data, String path) {
        save(data, new File(path));
    }

    /**
     * Dumps the data contents to a file to the given file
     *
     * @param file file to dump to
     */
    public void save(DataSection data, File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Couldn't save config to " + file.getName(), e);
            }
        }
        try (FileOutputStream out = new FileOutputStream(file);
             BufferedWriter write = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            save(data, write);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Dumps the data contents into the stream
     *
     * @param write stream to dump to
     * @throws IOException
     */
    public void save(DataSection data, BufferedWriter write) throws IOException {
        StringBuilder sb = new StringBuilder();
        dump(data, sb, 0, '\'');
        write.write(sb.toString());
    }

    /**
     * Dumps config data to a string in YAML format
     *
     * @param data    data to dump
     * @param builder string builder to use
     * @param indent  starting indent
     * @param quote   character to use for containing strings
     */
    public void dump(DataSection data, StringBuilder builder, int indent, char quote) {
        // Create spacing to use
        String spacing = "";
        for (int i = 0; i < indent; i++) {
            spacing += ' ';
        }

        for (String key : data.keys()) {
            // Comments first
            if (data.hasComment(key)) {
                List<String> lines = data.getComments(key);
                for (String line : lines) {
                    if (line.length() == 0) {
                        builder.append('\n');
                        continue;
                    }
                    builder.append(spacing);
                    builder.append('#');
                    builder.append(line);
                    builder.append('\n');
                }
            }

            // Write the key
            builder.append(spacing);
            if (key.matches("^[a-zA-Z0-9_]+$")) {
                builder.append(key);
            } else {
                if (key.contains(quote + "")) {
                    String tempKey   = key;
                    String tempQuote = "\"";
                    if (tempKey.contains(tempQuote)) {
                        tempKey = tempKey.replace(tempQuote + "", "\\" + tempQuote);
                    }
                    builder.append(tempQuote).append(tempKey).append(tempQuote);
                } else {
                    builder.append(quote).append(key).append(quote);
                }
            }
            builder.append(": ");

            Object value = data.get(key);

            // Empty section
            if (value == null) {
                builder.append("{}\n");
            }

            // Section with content
            else if (value instanceof DataSection) {
                DataSection child = (DataSection) value;
                if (child.size() == 0) {
                    builder.append("{}\n");
                } else {
                    builder.append('\n');
                    dump(child, builder, indent + 2, quote);
                }
            }

            // List value
            else if (value instanceof List) {
                List list = (List) value;
                if (list.size() == 0) {
                    builder.append("[]");
                    builder.append('\n');
                } else {
                    builder.append('\n');
                    for (Object item : list) {
                        builder.append(spacing);
                        builder.append("- ");
                        writeValue(builder, item, quote);
                        builder.append('\n');
                    }
                }
            }

            // Single value
            else {
                writeValue(builder, value, quote);
                builder.append('\n');
            }
        }
    }

    private void writeValue(StringBuilder builder, Object value, char quote) {
        if (value instanceof Number) {
            builder.append(value);
        } else if (value.toString().contains("" + quote)) {
            builder.append('"');
            builder.append(value);
            builder.append('"');
        } else {
            builder.append(quote);
            builder.append(value);
            builder.append(quote);
        }
    }
}
