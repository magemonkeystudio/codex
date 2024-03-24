package com.promcteam.codex.commands.list;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.commands.api.ISubCommand;
import com.promcteam.codex.utils.ClickText;
import com.promcteam.codex.utils.ItemUT;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Base64Command extends ISubCommand<CodexEngine> {

    public Base64Command(@NotNull CodexEngine plugin) {
        super(plugin, new String[]{"base64"}, "fcore.admin");
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return "Converts item to Base64";
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player    p    = (Player) sender;
        ItemStack main = p.getInventory().getItemInMainHand();

        if (ItemUT.isAir(main)) {
            this.errItem(sender);
            return;
        }

        String b64 = ItemUT.toBase64(main);
        if (b64 == null) {
            sender.sendMessage("Unexpected error!");
            return;
        }

        ClickText clickText = new ClickText("&6*** &eItem converted (hover):&a %button% &6***");
        clickText.createPlaceholder("%button%", "[Base64]")
                .hint("&e" + b64, "&bClick to print in console.")
                .execCmd("/" + b64)
        ;
        clickText.send(p);
    }
}
