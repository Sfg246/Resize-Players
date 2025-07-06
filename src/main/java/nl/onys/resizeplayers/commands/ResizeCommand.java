package nl.onys.resizeplayers.commands;

import nl.onys.resizeplayers.utils.MessageUtils;
import nl.onys.resizeplayers.utils.ScaleUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ResizeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /resize <blocks> [player/all]");
            return false;
        }

        // /resize <size>
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                MessageUtils.onOnlyPlayers(sender);
                return true;
            }
            return resizeSelf(player, args[0]);
        }

        // /resize <size> all
        if (args[1].equalsIgnoreCase("all")) {
            if (sender instanceof Player player) {
                return resizeAll(player, args[0]);
            } else {
                return resizeAll(null, args[0]);
            }
        }

        // /resize <size> <player>
        return resizeOther(sender, args[0], args[1]);
    }

    private boolean resizeSelf(Player player, String size) {
        if (!player.hasPermission("resizeplayers.scale.self") && !player.isOp()) {
            MessageUtils.onNoPermission(player, "resizeplayers.scale.self");
            return false;
        }
        double[] sizes = validateAndConvertSize(player, size);
        if (sizes == null) return false;
        ScaleUtils.setPlayerScale(player, sizes[1], true, true);
        MessageUtils.onScaledSelf(player, sizes[0]);
        return true;
    }

    private boolean resizeOther(CommandSender sender, String size, String target) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null) {
            if (sender != null) MessageUtils.onPlayerNotFound(sender, target);
            return false;
        }

        if (sender instanceof Player player) {
            if (!player.hasPermission("resizeplayers.scale.others") && !player.isOp()) {
                MessageUtils.onNoPermission(player, "resizeplayers.scale.others");
                return false;
            }
            if (targetPlayer.hasPermission("resizeplayers.scale.exempt") && player != targetPlayer) {
                MessageUtils.onTargetExempt(player, targetPlayer.getName());
                return false;
            }
        }

        double[] sizes = validateAndConvertSize(sender, size);
        if (sizes == null) return false;
        ScaleUtils.setPlayerScale(targetPlayer, sizes[1], true, true);

        if (sender instanceof Player player) {
            MessageUtils.onScaledOther(player, sizes[0], targetPlayer.getName());
        }

        return true;
    }

    private boolean resizeAll(Player player, String size) {
        if (player != null && !player.hasPermission("resizeplayers.scale.all") && !player.isOp()) {
            MessageUtils.onNoPermission(player, "resizeplayers.scale.all");
            return false;
        }

        double[] sizes = validateAndConvertSize(player, size);
        if (sizes == null) return false;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("resizeplayers.scale.exempt")) {
                if (player != null) MessageUtils.onTargetExempt(player, onlinePlayer.getName());
                continue;
            }
            ScaleUtils.setPlayerScale(onlinePlayer, sizes[1], true, true);
        }

        if (player != null) MessageUtils.onScaledAll(player, sizes[0]);

        return true;
    }

    private double[] validateAndConvertSize(CommandSender sender, String size) {
        if (!ScaleUtils.isValidSize(sender, size)) {
            return null;
        }
        double blocksSize;
        double scaleSize;
        if (size.equalsIgnoreCase("default")) {
            scaleSize = 1.0;
            blocksSize = ScaleUtils.convertScaleToBlocks(scaleSize);
        } else {
            blocksSize = Double.parseDouble(size);
            scaleSize = ScaleUtils.convertBlocksToScale(blocksSize);
        }
        return new double[]{blocksSize, scaleSize};
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        switch (args.length) {
            case 1: {
                String[] defaultOptions = {"0.5", "0.75", "1", "1.25", "1.5", "1.75", "2", "2.25", "2.5", "2.75", "3", "default"};
                String filter = args[0].toLowerCase();
                List<String> optionsList = new ArrayList<>();
                for (String option : defaultOptions) {
                    if (option.toLowerCase().startsWith(filter)) {
                        optionsList.add(option);
                    }
                }
                return optionsList;
            }
            case 2: {
                String filter = args[1].toLowerCase();
                List<String> playerNames = new ArrayList<>();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(filter)) {
                        playerNames.add(onlinePlayer.getName());
                    }
                }
                playerNames.add("all");
                return playerNames;
            }
            default:
                return null;
        }
    }
}
