package com.mcextractors.init;

import com.mcextractors.MCExtractors;
import com.mcextractors.menu.IronExtractorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, MCExtractors.MOD_ID);

    public static final RegistryObject<MenuType<IronExtractorMenu>> IRON_EXTRACTOR =
        MENUS.register("iron_extractor", () ->
            IForgeMenuType.create(IronExtractorMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
