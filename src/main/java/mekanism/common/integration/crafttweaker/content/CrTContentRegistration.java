package mekanism.common.integration.crafttweaker.content;

final class CrTContentRegistration {

    private final Runnable registration;
    private boolean mekanismReady;
    private boolean craftTweakerReady;
    private boolean started;

    CrTContentRegistration(Runnable registration) {
        this.registration = registration;
    }

    void onMekanismReady() {
        mekanismReady = true;
        registerWhenReady();
    }

    void onCraftTweakerReady() {
        craftTweakerReady = true;
        registerWhenReady();
    }

    private void registerWhenReady() {
        if (mekanismReady && craftTweakerReady && !started) {
            started = true;
            registration.run();
        }
    }
}
