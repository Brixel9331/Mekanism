package mekanism.common.registration;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public class DoubleDeferredRegister<PRIMARY, SECONDARY> {

    private final DeferredRegister<PRIMARY> primaryRegister;
    private final DeferredRegister<SECONDARY> secondaryRegister;

    public DoubleDeferredRegister(DeferredRegister<PRIMARY> primaryRegistry, DeferredRegister<SECONDARY> secondaryRegistry) {
        this.primaryRegister = primaryRegistry;
        this.secondaryRegister = secondaryRegistry;
    }

    public DoubleDeferredRegister(String modid, Registry<PRIMARY> primaryRegistry, Registry<SECONDARY> secondaryRegistry) {
        this(new DeferredRegister<>(modid, primaryRegistry.key(), () -> primaryRegistry),
              new DeferredRegister<>(modid, secondaryRegistry.key(), () -> secondaryRegistry));
    }

    public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<P, S>> W register(String name, Supplier<? extends P> primarySupplier,
          Supplier<? extends S> secondarySupplier, BiFunction<RegistryObject<P>, RegistryObject<S>, W> objectWrapper) {
        return objectWrapper.apply(primaryRegister.registerEntry(name, primarySupplier), secondaryRegister.registerEntry(name, secondarySupplier));
    }

    public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<P, S>> W register(String name, Supplier<? extends P> primarySupplier,
          Function<P, S> secondarySupplier, BiFunction<RegistryObject<P>, RegistryObject<S>, W> objectWrapper) {
        return registerAdvanced(name, primarySupplier, secondarySupplier.compose(RegistryObject::get), objectWrapper);
    }

    public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<P, S>> W registerAdvanced(String name, Supplier<? extends P> primarySupplier,
          Function<RegistryObject<P>, S> secondarySupplier, BiFunction<RegistryObject<P>, RegistryObject<S>, W> objectWrapper) {
        RegistryObject<P> primaryObject = primaryRegister.registerEntry(name, primarySupplier);
        return objectWrapper.apply(primaryObject, secondaryRegister.registerEntry(name, () -> secondarySupplier.apply(primaryObject)));
    }

    public void register() {
        primaryRegister.register();
        secondaryRegister.register();
    }
}