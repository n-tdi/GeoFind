package me.bubbles.geofind.requests;

import me.bubbles.geofind.GeoFind;
import me.bubbles.geofind.users.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.text.DecimalFormat;

public class Request {

    private GeoFind plugin;
    private User sender;
    private User recipient;
    private int ticks;

    public Request(GeoFind plugin, User sender, User recipient) {
        this.plugin=plugin;
        this.sender=sender;
        this.recipient=recipient;
        this.ticks=0;
        if(externalFactors())
            end();
    }

    private boolean externalFactors() {
        if(sender.getPlayer().hasPermission("geofind.bypass")) {
            sender.sendMessage(getLocationMessage());
        } else if(recipient.getBlocklist().contains(Bukkit.getOfflinePlayer(sender.getPlayer().getUniqueId()))) {
            sender.sendMessage("%prefix% %primary%You cannot send a request to %secondary%"+recipient.getPlayer().getName()+"%primary%.");
        } else if(recipient.getWhitelist().contains(Bukkit.getOfflinePlayer(sender.getPlayer().getUniqueId()))) {
            complete();
            return false;
        } else {
            sender.sendMessage("%prefix% %primary%Request sent to" + "%secondary%" + recipient.getPlayer().getName() + "%primary%.");
            String message = "%prefix% %primary%New request from %secondary%" + sender.getPlayer().getName() + "%primary%." +
                    "\n%primary%Use %secondary%/geodecline "+sender.getPlayer().getName()+"%primary% to decline this request," +
                    "or %secondary%/geoaccept "+sender.getPlayer().getName()+" %primary%to accept this request.";
            recipient.sendMessage(
                    "%prefix% %primary%New request from %secondary%" + sender.getPlayer().getName() + "%primary%." +
                            "\n%primary%Use %secondary%/geodecline <player> %primary%to decline this request, or %secondary%/geoaccept <player> %primary%to accept this request."
            );
            return false;
        }
        return true;
    }

    public void onTick() {
        ticks=clamp(ticks,ticks+1,0,plugin.getRequestManager().getRequestTimeout());
        if(ticks==plugin.getRequestManager().getRequestTimeout()) {
            expire();
        }
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    private int clamp(int old, int now, int min, int max) {
        if(now<min) {
            return Math.max(old, min);
        }
        if(now>max) {
            return Math.min(old, max);
        }
        return now;
    }

    private String getLocationMessage() {
        Location d1 = sender.getPlayer().getLocation();
        Location d2 = recipient.getPlayer().getLocation();
        double a = d1.getBlockX() - d2.getBlockX();
        double b = d1.getBlockZ() - d2.getBlockZ();
        double distance = Math.sqrt((a*a)+(b*b));
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        String text = ChatColor.translateAlternateColorCodes('&',
                "%prefix% %primary%" + sender.getPlayer().getName() +
                        "\n%primary%Distance: %secondary%" + df.format(distance) + "m" +
                        "\n%primary%World: %secondary%" + d2.getWorld().getName() +
                        "\n%primary%X: %secondary%" + d2.getBlockX() + " %primary%Y: %secondary%" + d2.getBlockY() + " %primary%Z: %secondary%" + d2.getBlockZ());
        return text;
    }

    public void accept() {
        recipient.sendMessage("%prefix% %primary%You have accepted the request from %secondary%"+sender.getPlayer().getName()+"%primary%.");
        complete();
    }

    public void decline() {
        recipient.sendMessage("%prefix% %primary%You have declined the request from %secondary%"+sender.getPlayer().getName()+"%primary%.");
        sender.sendMessage("%prefix% %secondary%"+recipient.getPlayer().getName()+"%primary% has declined your request.");
        end();
    }

    private void complete() {
        sender.sendMessage(getLocationMessage());
        recipient.sendMessage(
                "%prefix% %secondary%" + sender.getPlayer().getName() + "%primary% has been sent your location."
        );
        end();
    }

    public void cancel() {
        sender.sendMessage(
                "%prefix% %primary%Request to %secondary%" + recipient.getPlayer().getName() + " %primary%has been cancelled."
        );
        recipient.sendMessage(
                "%prefix% %primary%The request from %secondary%" + sender.getPlayer().getName() + "%primary% has been cancelled by the sender."
        );
        end();
    }

    private void expire() {
        sender.sendMessage(
                "%prefix% %primary%Request to %secondary%" + recipient.getPlayer().getName() + " %primary%has expired."
        );
        recipient.sendMessage(
                "%prefix% %primary%The request from %secondary%" + sender.getPlayer().getName() + "%primary% has expired."
        );
        end();
    }

    private void end() {
        plugin.getRequestManager().removeRequest(this);
    }

}
