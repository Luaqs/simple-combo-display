package dev.luaq.combos;

import dev.luaq.combos.profiles.Hypixel;
import dev.luaq.combos.profiles.Minemen;
import dev.luaq.combos.profiles.Profile;
import dev.luaq.combos.profiles.PvpLand;
import dev.luaq.combos.util.ChatUtils;
import dev.luaq.combos.util.ComboHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Mod(modid = "combos", version = "1.0")
public class Combos {
    @Mod.Instance
    public static Combos INSTANCE;

    private final List<Profile> profiles = new ArrayList<>();

    private Minecraft mc;
    private Profile detection;
    private boolean lastIsRound;

    private int fightingEID = -1;
    private int combo = 0;
    private int highest = 0;
    private int totalHits = 0;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        mc = Minecraft.getMinecraft();
        detection = null;

        profiles.add(new Hypixel());
        profiles.add(new Minemen());
        profiles.add(new PvpLand());

        // register all events here
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onConnection(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (mc.isSingleplayer()) {
            return;
        }

        // set the detection to null
        detection = null;

        String remoteAddr = event.manager.getRemoteAddress().toString();
        // go through each profile and check its IP regex
        for (Profile profile : profiles) {
            Matcher addressMatcher = profile.getIpRegex().matcher(remoteAddr);
            if (!addressMatcher.find()) {
                continue;
            }

            // found the right profile
            detection = profile;
            break;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (detection == null) {
            return;
        }

        // reset the combo and make inRound false
        detection.setInRound(false);
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        detection = null;
        resetRoundCombo();
    }

    @SubscribeEvent
    public void onMessage(ClientChatReceivedEvent event) {
        if (detection == null) {
            return; // if no detection then null
        }

        // the chat message with no color codes
        String unformattedText = event.message.getUnformattedText();
        unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(unformattedText);

        // send the message through the detection
        detection.handleMessage(unformattedText);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld == null || detection == null) {
            return; // ignore world ticks
        }

        ChannelPipeline pipe = mc.thePlayer.sendQueue.getNetworkManager().channel().pipeline();
        // add the "combo handler" before packet handler if not added already
        if (pipe.get("combo_handler") == null && pipe.get("packet_handler") != null) {
            pipe.addBefore("packet_handler", "combo_handler", new ComboHandler());
        }

        boolean newInRound = detection.inRound();
        if (lastIsRound && !newInRound) {
            // send the combo stats and
            // reset them
            sendComboStats();
            resetRoundCombo();
        }

        // update the last isRound
        lastIsRound = newInRound;
    }

    public void sendComboStats() {
        String message = "&r \n&d[%s Detection]\n&eLongest combo: &d%d\n&eTotal hits: &d%d\n&r ";
        // send a pretty message
        ChatUtils.sendMessage(String.format(message, detection.getName(), highest, totalHits));
    }

    public void resetCombo() {
        combo = 0;
        fightingEID = -1;
    }

    public void resetRoundCombo() {
        highest = 0;
        totalHits = 0;

        resetCombo();
    }

    @SubscribeEvent
    public void handleOutgoingHit(AttackEntityEvent event) {
        if (event.isCanceled() || event.entity != mc.thePlayer) {
            return; // if the hit was cancelled or attacker is not us
        }

        if (detection == null || !detection.inRound()) {
            return; // if not in a round or there's no detection
        }

        int targetId = event.target.getEntityId();
        if (fightingEID != targetId) {
            combo = 0;
            fightingEID = targetId;
        }
    }

    public void handleDamagePacket(S19PacketEntityStatus entityStatus) {
        // make sure it's damage and the detection is on
        if (entityStatus.getOpCode() != 2 || detection == null || !detection.inRound()) {
            return; // not a damage packet or detection is off
        }

        Entity fighting = entityStatus.getEntity(mc.theWorld);
        if (fighting == null) {
            // ignore the null player
            return;
        }

        if (fighting.getEntityId() == mc.thePlayer.getEntityId()) {
            // combo was cancelled :(
            resetCombo();
        } else if (fightingEID == fighting.getEntityId()) {
            totalHits++;

            // update the highest and the combo count
            highest = Math.max(highest, ++combo);
        }
    }
}
