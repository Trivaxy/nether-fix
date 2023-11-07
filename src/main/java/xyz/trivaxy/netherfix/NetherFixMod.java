package xyz.trivaxy.netherfix;

import com.fox2code.foxloader.loader.Mod;
import com.fox2code.foxloader.registry.*;

import java.awt.*;
import java.util.Random;
import java.util.logging.Logger;

public class NetherFixMod extends Mod {

    private static Logger LOGGER;

    @Override
    public void onPreInit() {
        LOGGER = getLogger();
    }

    public static Logger logger() {
        return LOGGER;
    }
}
