package com.promcteam.risecore.util.menu.replacements;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class TextReplacement extends MenuReplacement {
    @Getter
    @Setter
    private String value;

    public TextReplacement(String find, String value) {
        super(find);
        this.value = value;
    }

    @Override
    public String getValue(Player player) {
        return value;
    }
}
