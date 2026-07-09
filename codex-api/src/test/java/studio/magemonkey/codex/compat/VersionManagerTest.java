package studio.magemonkey.codex.compat;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class VersionManagerTest {

    @Test
    void getPackageFromVersion_supportsPurpur2612() throws Exception {
        Method method = VersionManager.class.getDeclaredMethod("getPackageFromVersion", String.class);
        method.setAccessible(true);

        Object packageName = method.invoke(null, "26.1.2");
        assert packageName.equals("v26_2");
    }
}
