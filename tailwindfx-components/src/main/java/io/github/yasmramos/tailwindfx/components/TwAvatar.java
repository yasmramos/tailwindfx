package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TwStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

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

  /** Default size modifier applied when none is supplied. */
  private static final String DEFAULT_SIZE = "md";

  /** Default color modifier applied when none is supplied. */
  private static final String DEFAULT_COLOR = "blue";

  /**
   * Diameter in pixels of each {@code .avatar-*} modifier. Kept aligned with {@code
   * tailwindfx-components.css} so clipping and image scaling match the styled container.
   */
  private static final Map<String, Double> AVATAR_SIZES =
      Map.of("xs", 24d, "sm", 32d, "md", 40d, "lg", 48d, "xl", 64d);

  /** Size modifier currently applied, tracked so wrappers can size themselves without CSS. */
  private String sizeModifier = DEFAULT_SIZE;

  /** Color modifier currently applied. */
  private String colorModifier = DEFAULT_COLOR;

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
    return new TwAvatar().applyInitials(initials, color, size);
  }

  private TwAvatar applyInitials(String initials, String color, String size) {
    String sizeKey = size == null || size.isEmpty() ? DEFAULT_SIZE : size;
    String colorKey = color == null || color.isEmpty() ? DEFAULT_COLOR : color;

    this.sizeModifier = sizeKey;
    this.colorModifier = colorKey;

    TwStyle.apply(this, "avatar", "avatar-" + sizeKey, "avatar-" + colorKey);
    setPadding(Insets.EMPTY);

    Label lbl = new Label(initials == null ? "" : initials.toUpperCase());
    TwStyle.apply(lbl, "avatar-text", "avatar-text-" + colorKey);

    getChildren().setAll(lbl);
    StackPane.setAlignment(lbl, Pos.CENTER);

    return this;
  }

  /**
   * Gets the applied size modifier.
   *
   * @return the size modifier (xs, sm, md, lg, xl)
   */
  public String getSizeModifier() {
    return sizeModifier;
  }

  /**
   * Gets the applied color modifier.
   *
   * @return the Tailwind color name
   */
  public String getColorModifier() {
    return colorModifier;
  }

  /**
   * Gets the avatar diameter in pixels for the applied size modifier.
   *
   * @return the avatar diameter in pixels
   */
  public double getAvatarSize() {
    return getAvatarSize(sizeModifier);
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
    if (image == null) {
      throw new IllegalArgumentException("image must not be null");
    }

    TwAvatar avatar = new TwAvatar();
    String sizeKey = size == null || size.isEmpty() ? DEFAULT_SIZE : size;
    avatar.sizeModifier = sizeKey;

    TwStyle.apply(avatar, "avatar", "avatar-" + sizeKey, "avatar-image");

    // Clip to a circle sized to the modifier so the image is cropped to the same box the CSS
    // gives the container.
    double avatarSize = getAvatarSize(sizeKey);
    Circle clip = new Circle(avatarSize / 2, avatarSize / 2, avatarSize / 2);
    avatar.setClip(clip);

    if (image instanceof ImageView imgView) {
      imgView.setFitWidth(avatarSize);
      imgView.setFitHeight(avatarSize);
      imgView.setPreserveRatio(true);
    }

    avatar.getChildren().setAll(image);

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

  /**
   * Resolves the pixel size of an avatar size modifier.
   *
   * <p>The values must stay in sync with the {@code .avatar-*} rules in {@code
   * tailwindfx-components.css}. They are used to build the circular clip and to scale images, so a
   * mismatch would crop or overflow the image relative to the styled container.
   *
   * @param size the size modifier (xs, sm, md, lg, xl)
   * @return the avatar diameter in pixels, defaulting to {@code md}
   */
  private static double getAvatarSize(String size) {
    if (size == null) {
      return AVATAR_SIZES.getOrDefault(DEFAULT_SIZE, 40d);
    }
    return AVATAR_SIZES.getOrDefault(size, AVATAR_SIZES.get(DEFAULT_SIZE));
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
        TwStyle.apply(avatar, "avatar-group-item");
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
      if (avatar == null) {
        throw new IllegalArgumentException("avatar must not be null");
      }
      this.avatar = avatar;
      getChildren().add(avatar);

      // Use the size tracked by the avatar rather than getMinWidth(): the CSS min-width is only
      // applied once the stylesheet is installed on the scene, so reading it here would silently
      // fall back to the default for every avatar built before that point.
      double size = avatar.getAvatarSize();

      statusDot = new Label();
      TwStyle.apply(
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
