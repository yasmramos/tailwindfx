package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TwAvatar component.
 */
public class TwAvatarTest extends ApplicationTest {

    @Override
    public void start(javafx.stage.Stage stage) {
        // Empty stage for TestFX
    }

    @Test
    public void testCreate_WithInitialsOnly() {
        TwAvatar avatar = TwAvatar.create("JD");
        
        assertNotNull(avatar);
        assertEquals(1, avatar.getChildren().size());
        assertTrue(avatar.getStyleClass().contains("avatar"));
        assertTrue(avatar.getStyleClass().contains("avatar-md"));
        assertTrue(avatar.getStyleClass().contains("avatar-blue"));
    }

    @Test
    public void testCreate_WithInitialsAndColor() {
        TwAvatar avatar = TwAvatar.create("AB", "red");
        
        assertNotNull(avatar);
        assertTrue(avatar.getStyleClass().contains("avatar-red"));
        assertFalse(avatar.getStyleClass().contains("avatar-blue"));
    }

    @Test
    public void testCreate_WithAllParameters() {
        TwAvatar avatar = TwAvatar.create("XY", "green", "lg");
        
        assertNotNull(avatar);
        assertTrue(avatar.getStyleClass().contains("avatar-lg"));
        assertTrue(avatar.getStyleClass().contains("avatar-green"));
    }

    @Test
    public void testCreate_DifferentSizes() {
        String[] sizes = {"xs", "sm", "md", "lg", "xl"};
        
        for (String size : sizes) {
            TwAvatar avatar = TwAvatar.create("T", "blue", size);
            assertTrue(avatar.getStyleClass().contains("avatar-" + size), 
                "Should contain avatar-" + size);
        }
    }

    @Test
    public void testFromImage_WithImageView() {
        Image image = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        ImageView imageView = new ImageView(image);
        
        TwAvatar avatar = TwAvatar.fromImage(imageView);
        
        assertNotNull(avatar);
        assertTrue(avatar.getStyleClass().contains("avatar"));
        assertTrue(avatar.getStyleClass().contains("avatar-image"));
        assertEquals(1, avatar.getChildren().size());
        assertNotNull(avatar.getClip());
    }

    @Test
    public void testFromImage_WithSize() {
        Image image = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        ImageView imageView = new ImageView(image);
        
        TwAvatar avatar = TwAvatar.fromImage(imageView, "xl");
        
        assertNotNull(avatar);
        assertTrue(avatar.getStyleClass().contains("avatar-xl"));
    }

    @Test
    public void testGroup_CreatesAvatarGroup() {
        TwAvatar avatar1 = TwAvatar.create("A");
        TwAvatar avatar2 = TwAvatar.create("B");
        TwAvatar avatar3 = TwAvatar.create("C");
        
        TwAvatar.TwAvatarGroup group = TwAvatar.group(avatar1, avatar2, avatar3);
        
        assertNotNull(group);
        assertEquals(3, group.getChildren().size());
    }

    @Test
    public void testWithStatus_CreatesAvatarWithStatus() {
        TwAvatar avatar = TwAvatar.create("JD");
        
        TwAvatar.TwAvatarWithStatus withStatusOnline = TwAvatar.withStatus(avatar, true);
        
        assertNotNull(withStatusOnline);
        assertEquals(avatar, withStatusOnline.getAvatar());
        assertNotNull(withStatusOnline.getStatusDot());
        assertTrue(withStatusOnline.getStatusDot().getStyleClass().contains("avatar-status-online"));
        assertFalse(withStatusOnline.getStatusDot().getStyleClass().contains("avatar-status-offline"));
    }

    @Test
    public void testWithStatus_Offline() {
        TwAvatar avatar = TwAvatar.create("JD");
        
        TwAvatar.TwAvatarWithStatus withStatusOffline = TwAvatar.withStatus(avatar, false);
        
        assertNotNull(withStatusOffline);
        assertTrue(withStatusOffline.getStatusDot().getStyleClass().contains("avatar-status-offline"));
        assertFalse(withStatusOffline.getStatusDot().getStyleClass().contains("avatar-status-online"));
    }

    @Test
    public void testAvatarGroup_ItemsHaveGroupClass() {
        TwAvatar avatar1 = TwAvatar.create("A");
        TwAvatar avatar2 = TwAvatar.create("B");
        
        TwAvatar.TwAvatarGroup group = new TwAvatar.TwAvatarGroup(avatar1, avatar2);
        
        assertTrue(avatar1.getStyleClass().contains("avatar-group-item"));
        assertTrue(avatar2.getStyleClass().contains("avatar-group-item"));
    }

    @Test
    public void testAvatarInitialials_AreUppercase() {
        TwAvatar avatar = TwAvatar.create("jd");
        
        Node child = avatar.getChildren().get(0);
        assertTrue(child instanceof javafx.scene.control.Label);
        javafx.scene.control.Label label = (javafx.scene.control.Label) child;
        assertEquals("JD", label.getText());
    }

    @Test
    public void testAvatarWithStatus_Positioning() {
        TwAvatar avatar = TwAvatar.create("JD", "blue", "md");
        
        TwAvatar.TwAvatarWithStatus withStatus = TwAvatar.withStatus(avatar, true);
        
        // Verify status dot is positioned
        assertNotNull(withStatus.getStatusDot());
        assertNotEquals(0, withStatus.getStatusDot().getTranslateX(), 0.01);
        assertNotEquals(0, withStatus.getStatusDot().getTranslateY(), 0.01);
    }
}
