package io.github.xfacthd.foup;

import com.mojang.logging.LogUtils;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.network.NetworkSetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Foup.MODID)
@SuppressWarnings("UtilityClassWithPublicConstructor")
public final class Foup
{
    public static final String MODID = "foup";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Foup(IEventBus modBus)
    {
        FoupContent.init(modBus);

        modBus.addListener(NetworkSetup::onRegisterPayloadHandlers);
    }
}
