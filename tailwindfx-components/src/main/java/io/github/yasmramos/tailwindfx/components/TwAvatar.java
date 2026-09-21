package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * TwAvatar — Pre-styled avatar component.
 *
 * <p>Uses base .avatar class from tailwindfx-components.css with utility modifiers.
 *
 * <pre>
 * TwAvatar avatar = TwAvatar.create("JD", "blue");
 * TwAvatar imgAvatar = TwAvatar.fromImage(imageView);
 * TwAvatarGroup group = TwAvatar.group(avatar1, avatar2, avatar3);
 * </pre>
 */
public class TwAvatar extends StackPane {

  /**
   * Creates an avatar with initials.
   *
   * @param initials the initials to display (e.g. "JD", "A")
   * @return styled TwAvatar with default blue color and 40px size
   */
  public static TwAvatar create(String initials) {
    return create(initials, "blue", "md");
  }

  /**
   * Creates an avatar with initials and custom color.
   *
   * @param initials the initials to display
   * @param color Tailwind color name
   * @return styled TwAvatar with 40px size
   */
  public static TwAvatar create(String initials, String color) {
    return create(initials, color, "md");
  }

  /**
   * Creates an avatar with initials, color, and custom size.
   *
   * @param initials the initials to display
   * @param color Tailwind color name
   * @param size Tailwind size modifier (xs, sm, md, lg, xl)
   * @return styled TwAvatar
   */
  public static TwAvatar create(String initials, String color, String size) {
    TwAvatar avatar = new TwAvatar();

    TailwindFX.apply(avatar, "avatar", "avatar-" + size, "avatar-" + color);
    avatar.setPadding(new Insets(0));

    Label lbl = new Label(initials.toUpperCase());
    TailwindFX.apply(lbl, "avatar-text", "avatar-text-" + color);

    avatar.getChildren().add(lbl);
    StackPane.setAlignment(lbl, Pos.CENTER);

    return avatar;
  }

  /**
   * Creates an avatar from an image node.
   *
   * @param image the image node (ImageView)
   * @return styled TwAvatar with 40px size
   */
  public static TwAvatar fromImage(Node image) {
    return fromImage(image, "md");
  }

  /**
   * Creates an avatar from an image node with custom size.
   *
   * @param image the image node (ImageView)
   * @param size Tailwind size modifier (xs, sm, md, lg, xl)
   * @return styled TwAvatar
   */
  public static TwAvatar fromImage(Node image, String size) {
    TwAvatar avatar = new TwAvatar();

    TailwindFX.apply(avatar, "avatar", "avatar-" + size, "avatar-image");

    // Clip to circle
    double avatarSize = getAvatarSize(size);
    javafx.scene.shape.Circle clip =
        new javafx.scene.shape.Circle(avatarSize / 2, avatarSize / 2, avatarSize / 2);
    avatar.setClip(clip);

    if (image instanceof javafx.scene.image.ImageView) {
      javafx.scene.image.ImageView imgView = (javafx.scene.image.ImageView) image;
      imgView.setFitWidth(avatarSize);
      imgView.setFitHeight(avatarSize);
      imgView.setPreserveRatio(true);
    }

    avatar.getChildren().add(image);

    return avatar;
  }

  /**
   * Creates an avatar group (overlapping avatars).
   *
   * @param avatars array of avatar nodes
   * @return TwAvatarGroup with overlapping avatars
   */
  public static TwAvatarGroup group(TwAvatar... avatars) {
    return new TwAvatarGroup(avatars);
  }

  /**
   * Creates an online status indicator for an avatar.
   *
   * @param avatar the avatar to wrap
   * @param isOnline true for online (green), false for offline (gray)
   * @return TwAvatarWithStatus containing avatar with status dot
   */
  public static TwAvatarWithStatus withStatus(TwAvatar avatar, boolean isOnline) {
    return new TwAvatarWithStatus(avatar, isOnline);
  }

  /** Protected constructor for internal usage. */
  protected TwAvatar() {
    super();
  }

  private static double getAvatarSize(String size) {
    switch (size) {
      case "xs":
        return 24;
      case "sm":
        return 32;
      case "md":
        return 40;
      case "lg":
        return 56;
      case "xl":
        return 72;
      default:
        return 40;
    }
  }

  /** Container for avatar group (overlapping avatars). */
  public static class TwAvatarGroup extends Pane {

    /**
     * Creates an avatar group with overlapping avatars.
     *
     * @param avatars array of avatar nodes
     */
    public TwAvatarGroup(TwAvatar... avatars) {
      super();
      double spacing = -12; // Overlap

      double xOffset = 0;
      for (TwAvatar avatar : avatars) {
        // Add border class to each avatar in group
        TailwindFX.apply(avatar, "avatar-group-item");
        avatar.setTranslateX(xOffset);
        getChildren().add(avatar);
        xOffset += spacing;
      }
    }
  }

  /** Container for avatar with status indicator. */
  public static class TwAvatarWithStatus extends StackPane {

    private final TwAvatar avatar;
    private final Label statusDot;

    /**
     * Creates an avatar with status indicator.
     *
     * @param avatar the avatar to wrap
     * @param isOnline true for online (green), false for offline (gray)
     */
    public TwAvatarWithStatus(TwAvatar avatar, boolean isOnline) {
      super();
      this.avatar = avatar;
      getChildren().add(avatar);

      double size = avatar.getMinWidth();
      if (size <= 0) size = 40; // default

      statusDot = new Label();
      TailwindFX.apply(
          statusDot,
          "avatar-status-dot",
          isOnline ? "avatar-status-online" : "avatar-status-offline");

      StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);
      getChildren().add(statusDot);

      // Adjust position
      statusDot.setTranslateX(size * 0.15);
      statusDot.setTranslateY(size * 0.15);
    }

    /**
     * Gets the wrapped avatar.
     *
     * @return the TwAvatar
     */
    public TwAvatar getAvatar() {
      return avatar;
    }

    /**
     * Gets the status dot.
     *
     * @return the Circle status indicator
     */
    public Label getStatusDot() {
      return statusDot;
    }
  }
}
