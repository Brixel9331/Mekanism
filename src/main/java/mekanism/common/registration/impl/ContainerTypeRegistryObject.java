package mekanism.common.registration.impl;

import mekanism.api.MekanismAPI;
import mekanism.api.text.ILangEntry;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.registration.RegistryObject;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ContainerTypeRegistryObject<CONTAINER extends AbstractContainerMenu> extends RegistryObject<MenuType<CONTAINER>> {

    public ContainerTypeRegistryObject(RegistryObject<MenuType<CONTAINER>> registryObject) {
        super(registryObject);
    }

    @Nullable
    public MenuProvider getProvider(ILangEntry name, Object object) {
        return getProvider(name.translate(), object);
    }

    @Nullable
    public MenuProvider getProvider(Component name, Object object) {
        MenuConstructor provider = null;
        MenuType<CONTAINER> containerType = get();
        if (containerType instanceof MekanismContainerType<?, CONTAINER> mekanismContainerType) {
            provider = mekanismContainerType.create(object);
        }
        if (provider == null) {
            MekanismAPI.logger.info("Unable to create container for type: {}", RegistryUtils.getName(containerType));
        }
        return provider == null ? null : new ContainerProvider(name, provider);
    }

    @Nullable
    public MenuProvider getProvider(ILangEntry name, InteractionHand hand, ItemStack stack) {
        return getProvider(name.translate(), hand, stack);
    }

    @Nullable
    public MenuProvider getProvider(Component name, InteractionHand hand, ItemStack stack) {
        MenuConstructor provider = null;
        MenuType<CONTAINER> containerType = get();
        if (containerType instanceof MekanismItemContainerType<?, ?> mekanismItemContainerType) {
            provider = mekanismItemContainerType.create(hand, stack);
        }
        if (provider == null) {
            MekanismAPI.logger.info("Unable to create container for type: {}", RegistryUtils.getName(containerType));
        }
        return provider == null ? null : new ContainerProvider(name, provider);
    }

    public void tryOpenGui(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        MenuProvider provider = getProvider(stack.getHoverName(), hand, stack);
        if (provider != null) {
            //Validate the provider isn't null, it shouldn't be but just in case
            ContainerProvider.open(player, provider, buf -> {
                buf.writeEnum(hand);
                buf.writeItem(stack);
            });
        }
    }
}