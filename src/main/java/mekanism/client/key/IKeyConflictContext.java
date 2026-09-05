package mekanism.client.key;

@FunctionalInterface
public interface IKeyConflictContext {

    boolean isActive();

    default boolean conflicts(IKeyConflictContext other) {
        return this == other || this == KeyConflictContext.UNIVERSAL || other == KeyConflictContext.UNIVERSAL;
    }
}
