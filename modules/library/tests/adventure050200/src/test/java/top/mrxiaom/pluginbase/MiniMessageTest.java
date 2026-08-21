package top.mrxiaom.pluginbase;

import org.junit.jupiter.api.Test;

public class MiniMessageTest {
    private final MiniMessageTestShared shared = new MiniMessageTestShared("5.2.0");
    @Test
    public void basicFormatting() {
        shared.basicFormatting();
    }

    @Test
    public void colorTransitions() {
        shared.colorTransitions();
    }

    @Test
    public void interactiveEvents() {
        shared.interactiveEvents();
    }

    @Test
    public void componentReferences() {
        shared.componentReferences();
    }

    @Test
    public void modernComponents() {
        shared.modernComponents();
    }
}
