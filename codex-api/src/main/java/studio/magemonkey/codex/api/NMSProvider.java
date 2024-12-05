package studio.magemonkey.codex.api;

import lombok.Setter;

public class NMSProvider {
    @Setter
    protected static NMS nms;

    public static NMS getNms() {
        if (nms == null) {
            throw new RuntimeException("NMS has not been set yet! Something is wrong.");
        }

        return nms;
    }
}
