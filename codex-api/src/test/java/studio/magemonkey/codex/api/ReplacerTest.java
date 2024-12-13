package studio.magemonkey.codex.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplacerTest {
    private Replacer replacer;

    @BeforeEach
    void setUp() {
        replacer = new Replacer("from", "to");
    }

    @Test
    void use() {
        String str    = "get this from";
        String result = replacer.use(str);
        assert result.equals("get this to");
    }

    @Test
    void replacer_constructedWithNullGivesStringNull() {
        Replacer replacer = Replacer.replacer(null, (String) null);
        assert replacer.getFrom().equals("null");
        assert replacer.getTo().equals("null");
    }

    @Test
    void replacer_supplierConstructor() {
        Replacer replacer = Replacer.replacer("from", () -> "to");
        assert replacer.getFrom().equals("from");
        assert replacer.getTo().equals("to");
    }
}