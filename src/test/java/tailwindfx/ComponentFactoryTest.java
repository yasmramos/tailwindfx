package tailwindfx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComponentFactory - non-UI methods only.
 * Tests requiring JavaFX UI components should use TestFX integration tests.
 */
public class ComponentFactoryTest {

    @Test
    public void testComponentFactory_classLoads() {
        // ComponentFactory has private constructor, but we can verify it loads properly
        assertDoesNotThrow(() -> {
            Class<?> clazz = ComponentFactory.class;
            assertNotNull(clazz);
        });
    }

    @Test
    public void testDrawerSide_enum() {
        // Test that DrawerSide enum values exist
        ComponentFactory.DrawerSide left = ComponentFactory.DrawerSide.LEFT;
        ComponentFactory.DrawerSide right = ComponentFactory.DrawerSide.RIGHT;
        ComponentFactory.DrawerSide top = ComponentFactory.DrawerSide.TOP;
        ComponentFactory.DrawerSide bottom = ComponentFactory.DrawerSide.BOTTOM;

        assertNotNull(left);
        assertNotNull(right);
        assertNotNull(top);
        assertNotNull(bottom);
    }

    @Test
    public void testCardBuilder_builderCreation() {
        // Test that builder can be created (without calling build() which needs JavaFX)
        ComponentFactory.CardBuilder builder = ComponentFactory.card();
        assertNotNull(builder);

        // Test fluent interface returns same builder
        ComponentFactory.CardBuilder result = builder.title("Test");
        assertSame(builder, result);

        result = builder.body(null);
        assertSame(builder, result);

        result = builder.footer(null);
        assertSame(builder, result);

        result = builder.shadow(true);
        assertSame(builder, result);

        result = builder.border(false);
        assertSame(builder, result);

        result = builder.hoverable(true);
        assertSame(builder, result);

        result = builder.padding(8);
        assertSame(builder, result);

        result = builder.radius(16);
        assertSame(builder, result);
    }

    @Test
    public void testModalBuilder_builderCreation() {
        // Note: We can't test modal() method without a Node parameter
        // This test just verifies the ModalBuilder class exists
        assertNotNull(ComponentFactory.ModalBuilder.class);
    }

    @Test
    public void testDrawerBuilder_builderCreation() {
        // Test that drawer builder can be created
        ComponentFactory.DrawerBuilder builder = ComponentFactory.drawer(
            ComponentFactory.DrawerSide.LEFT, 300);
        assertNotNull(builder);

        // Test fluent interface
        ComponentFactory.DrawerBuilder result = builder.animated(true);
        assertSame(builder, result);

        result = builder.duration(250);
        assertSame(builder, result);
    }

    @Test
    public void testGlassBuilder_builderCreation() {
        ComponentFactory.GlassBuilder builder = ComponentFactory.glass();
        assertNotNull(builder);

        // Test all fluent methods
        ComponentFactory.GlassBuilder result = builder.blur(12);
        assertSame(builder, result);

        result = builder.opacity(0.25);
        assertSame(builder, result);

        result = builder.border(true);
        assertSame(builder, result);

        result = builder.borderColor("rgba(255,255,255,0.3)");
        assertSame(builder, result);

        result = builder.padding(20);
        assertSame(builder, result);

        result = builder.radius(16);
        assertSame(builder, result);
    }

    @Test
    public void testNeumorphicBuilder_builderCreation() {
        // Note: neumorphic() requires a Region parameter
        // This test just verifies the class exists
        assertNotNull(ComponentFactory.NeumorphicBuilder.class);
    }

    @Test
    public void testDataTableBuilder_builderCreation() {
        // Note: dataTable() requires a Class parameter
        // This test just verifies the class exists
        assertNotNull(ComponentFactory.DataTableBuilder.class);
    }
}