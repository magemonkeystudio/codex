package mc.promcteam.engine.mccore.config.parse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YAMLParserTest {
    private static YAMLParser yamlParser = new YAMLParser();

    @Test
    void parseText_normalText() {
        String text = "test:\n    one: 'one-txt'\n    two: 'two-txt'\n    three: 'three-txt'\nother-item: 'other-txt'";

        DataSection data = yamlParser.parseText(text);

        assertEquals("one-txt", data.getString("test.one"));
        assertEquals("two-txt", data.getString("test.two"));
        assertEquals("three-txt", data.getString("test.three"));
        assertEquals("other-txt", data.getString("other-item"));
    }

    @Test
    void parseText_listsParse() {
        String text = "this-list: [one, two, three]\nother-list:\n- one\n- two\n- three\nthird-list: ['quoted-one', 'quoted-two', 'quoted-three']";

        DataSection data = yamlParser.parseText(text);

        assertEquals("one", data.getList("this-list").get(0));
        assertEquals("two", data.getList("other-list").get(1));
        assertEquals("quoted-three", data.getList("third-list").get(2));
    }

    @Test
    void parseText_indentedListParses() {
        String text = "other-list:\n  - one\n  - two\n  - three";

        DataSection data = yamlParser.parseText(text);

        assertEquals("one", data.getList("other-list").get(0));
        assertEquals("two", data.getList("other-list").get(1));
        assertEquals("three", data.getList("other-list").get(2));
    }

    @Test
    void parseText_parsesNonPipedMultilineString() {
        String text = "multiline: \"This is a\n  multiline\n  string\n  that spans\n  multiple lines.\"";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multiline string that spans multiple lines.", data.getString("multiline"));
    }

    @Test
    void parseText_parsesPipedMultilineString() {
        String text = "multiline: |\n  This is a\n  multiline\n  string\n  that spans\n  multiple lines. ";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a\nmultiline\nstring\nthat spans\nmultiple lines. ", data.getString("multiline"));
    }

    @Test
    void parseText_parsesFoldedMultilineString() {
        String text = "multiline: >\n  This is a\n  multiline\n  string\n  that spans\n  multiple lines.";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multiline string that spans multiple lines.", data.getString("multiline"));
    }
}