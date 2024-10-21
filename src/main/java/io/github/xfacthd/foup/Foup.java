package io.github.xfacthd.foup;

import com.mojang.logging.LogUtils;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.command.FoupCommands;
import io.github.xfacthd.foup.common.data.capability.CapabilitySetup;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugPayloads;
import io.github.xfacthd.foup.common.network.NetworkSetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Foup.MOD_ID)
@SuppressWarnings("UtilityClassWithPublicConstructor")
public final class Foup
{
    public static final String MOD_ID = "foup";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Foup(IEventBus modBus)
    {
        FoupContent.init(modBus);

        modBus.addListener(NetworkSetup::onRegisterPayloadHandlers);
        modBus.addListener(CapabilitySetup::onRegisterCapabilities);

        NeoForge.EVENT_BUS.addListener(FoupCommands::registerCommands);

        if (!FMLEnvironment.production)
        {
            NeoForge.EVENT_BUS.addListener(RailNetworkDebugPayloads::onServerTickEnd);
            NeoForge.EVENT_BUS.addListener(RailNetworkDebugPayloads::onPlayerDisconnect);
        }
    }
}
