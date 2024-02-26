package mc.promcteam.engine.mccore.config.parse;

import lombok.extern.slf4j.Slf4j;
import mc.promcteam.engine.NexEngine;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Slf4j
class YAMLParserTest {
    private static YAMLParser              yamlParser = new YAMLParser();
    private static MockedStatic<NexEngine> engineMock;
    private static NexEngine               engine;
    private static Logger                  logger;

    @BeforeAll
    static void setUp() {
        engineMock = mockStatic(NexEngine.class);
        engine = mock(NexEngine.class);
        logger = mock(Logger.class);
        doAnswer(invocation -> {
            log.info(invocation.getArgument(0));
            return null;
        }).when(logger).info(anyString());
        engineMock.when(NexEngine::get).thenReturn(engine);
        when(engine.getLogger()).thenReturn(logger);
    }

    @AfterAll
    static void tearDown() {
        engineMock.close();
    }

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
        String text =
                "this-list: [one, two, three]\nother-list:\n- one\n- two\n- three\nthird-list: ['quoted-one', 'quoted-two', 'quoted-three']";

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
        String text =
                "multiline: \"This is a\n  multiline\n  string\n  that spans\n  multiple lines.\"\ndummy: data after";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multiline string that spans multiple lines.", data.getString("multiline"));
        assertEquals("data after", data.getString("dummy"));
    }

    @Test
    void parseText_parsesPipedMultilineString() {
        String text = "multiline: |\n  This is a\n  multiline\n  string\n  that spans\n  multiple lines. ";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a\nmultiline\nstring\nthat spans\nmultiple lines. ", data.getString("multiline"));
    }

    @Test
    void parseText_parsesFoldedMultilineString() {
        String text =
                "multiline: >\n  This is a\n  multiline\n  string\n  that spans\n  multiple lines.\ndummy: data after";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multiline string that spans multiple lines.", data.getString("multiline"));
    }

    @Test
    void parseText_parsesMultilineWithoutQuotes() {
        String text =
                "multiline: This is a\n  multi: line\n  string\n  that spans\n  multiple lines.\ndummy: data after";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multi: line string that spans multiple lines.", data.getString("multiline"));
    }

    @Test
    void parseText_parsesSectionIfKeyIsInQuotes() {
        String text = "\"quoted-key\": \nkey: 'value'";

        DataSection data = yamlParser.parseText(text);

        assertNotNull(data.getSection("quoted-key"));
        assertEquals("value", data.getString("key"));
    }

    @Test
    void dump_quotesKeyIfNonAlphaNumeric() {
        DataSection data = new DataSection();
        data.set("key", "value");
        data.set("key with spaces", "value");
        data.set("key-with-dashes", "value");
        data.set("key_with_underscores", "value");
        data.set("key#with#hashes", "value");
        data.set("key$with$symbols", "value");
        data.set("key%with%symbols", "value");
        data.set("key^with^symbols", "value");
        data.set("key&with&symbols", "value");
        data.set("key*with*symbols", "value");
        data.set("key(with(symbols", "value");
        data.set("key)with)symbols", "value");
        data.set("key-with-symbols", "value");
        data.set("key=with=symbols", "value");
        data.set("key+with+symbols", "value");
        data.set("key\\with\\symbols", "value");
        data.set("key|with|symbols", "value");
        data.set("key`with`symbols", "value");
        data.set("key~with~symbols", "value");
        data.set("key!with!symbols", "value");
        data.set("key@with@symbols", "value");
        data.set("key:with:symbols", "value");
        data.set("key;with;symbols", "value");
        data.set("key'with'symbols", "value");
        data.set("key\"with'symbols", "value");
        data.set("key\"with\"symbols", "value");
        data.set("key<with<symbols", "value");
        data.set("key>with>symbols", "value");
        data.set("key,with,symbols", "value");
        data.set("key?with?symbols", "value");
        data.set("key/with/symbols", "value");
        data.set("key[with[symbols", "value");
        data.set("key]with]symbols", "value");
        data.set("key{with{symbols", "value");
        data.set("key}with}symbols", "value");
        data.set("key-1", "value");
        data.set("1key", "value");
        data.set("1key1", "value");

        StringBuilder builder = new StringBuilder();
        yamlParser.dump(data, builder, 0, '\'');

        String text = builder.toString();
        final String expected = "key: 'value'\n" + "'key with spaces': 'value'\n" + "'key-with-dashes': 'value'\n"
                + "key_with_underscores: 'value'\n" + "'key#with#hashes': 'value'\n" + "'key$with$symbols': 'value'\n"
                + "'key%with%symbols': 'value'\n" + "'key^with^symbols': 'value'\n" + "'key&with&symbols': 'value'\n"
                + "'key*with*symbols': 'value'\n" + "'key(with(symbols': 'value'\n" + "'key)with)symbols': 'value'\n"
                + "'key-with-symbols': 'value'\n" + "'key=with=symbols': 'value'\n" + "'key+with+symbols': 'value'\n"
                + "'key\\with\\symbols': 'value'\n" + "'key|with|symbols': 'value'\n" + "'key`with`symbols': 'value'\n"
                + "'key~with~symbols': 'value'\n" + "'key!with!symbols': 'value'\n" + "'key@with@symbols': 'value'\n"
                + "'key:with:symbols': 'value'\n" + "'key;with;symbols': 'value'\n" + "\"key'with'symbols\": 'value'\n"
                + "\"key\\\"with'symbols\": 'value'\n" + "'key\"with\"symbols': 'value'\n"
                + "'key<with<symbols': 'value'\n" + "'key>with>symbols': 'value'\n" + "'key,with,symbols': 'value'\n"
                + "'key?with?symbols': 'value'\n" + "'key/with/symbols': 'value'\n" + "'key[with[symbols': 'value'\n"
                + "'key]with]symbols': 'value'\n" + "'key{with{symbols': 'value'\n" + "'key}with}symbols': 'value'\n"
                + "'key-1': 'value'\n" + "1key: 'value'\n" + "1key1: 'value'\n";
        assertEquals(expected, text);
    }

    @Test
    void parseText_list_parsesWithMultilineEntry() {
        String text =
                "list:\n  - This is a\n    multi: line\n    entry\n  - This is a single line entry\ndummy: data after";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multi: line entry", data.getList("list").get(0));
        assertEquals("This is a single line entry", data.getList("list").get(1));
    }

    @Test
    void parseText_list_parsesWithQuotedMultilineEntry() {
        String text =
                "list:\n  - 'This is a\n    multi: line\n    entry'\n  - This is a single line entry\ndummy: data after";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multi: line entry", data.getList("list").get(0));
        assertEquals("This is a single line entry", data.getList("list").get(1));
        assertEquals("data after", data.getString("dummy"));
    }

    @Test
    void parseText_list_parsesPipedMultilineEntry() {
        String text =
                "list:\n  - |\n    This is a\n    multi: line\n    entry\n  - This is a single line entry\ndummy: data after";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a\nmulti: line\nentry", data.getList("list").get(0));
        assertEquals("This is a single line entry", data.getList("list").get(1));
        assertEquals("data after", data.getString("dummy"));
    }

    @Test
    void parseText_list_parsesFoldedMultilineEntry() {
        String text =
                "list:\n  - >\n    This is a\n    multi: line\n    entry\n  - This is a single line entry\ndummy: data after";

        DataSection data = yamlParser.parseText(text);

        assertEquals("This is a multi: line entry", data.getList("list").get(0));
        assertEquals("This is a single line entry", data.getList("list").get(1));
        assertEquals("data after", data.getString("dummy"));
    }

    @Test
    void parse_parsesSapiDefaultConfig() {
        // Get sapiconfig from classpath
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("sapiconfig.yml");
        assertNotNull(in);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder builder = new StringBuilder();
            reader.lines().forEach(line -> builder.append(line).append("\n"));
            DataSection data = yamlParser.parseText(builder.toString());
            assertNotNull(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}