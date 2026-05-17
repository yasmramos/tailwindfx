package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TwStyle;
import io.github.yasmramos.tailwindfx.animation.FxAnimation;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * TwCard — Pre-styled card container component.
 * 
 * <pre>
 * VBox card = TwCard.create()
 *     .title("Revenue")
 *     .body(chart)
 *     .footer(actions)
 *     .build();
 * 
 * VBox simpleCard = TwCard.simple(content);
 * </pre>
 */
public final class TwCard {

    private TwCard() {}

    /**
     * Creates a card builder for complex cards.
     * @return CardBuilder instance
     */
    public static CardBuilder create() {
        return new CardBuilder();
    }

    /**
     * Creates a simple card with just content.
     * @param content the card content
     * @return styled VBox
     */
    public static VBox simple(Node content) {
        return create().body(content).build();
    }

    /**
     * Creates a card with title and content.
     * @param title card title
     * @param content card content
     * @return styled VBox
     */
    public static VBox withTitle(String title, Node content) {
        return create().title(title).body(content).build();
    }

    /**
     * Builder for styled card containers.
     */
    public static class CardBuilder {
        private String title = null;
        private Node body = null;
        private Node footer = null;
        private boolean shadow = true;
        private boolean border = false;
        private boolean hoverable = false;
        private double padding = 4; // Tailwind units
        private double radius = 12; // px

        /**
         * Sets the card title.
         * @param t title text
         * @return this builder
         */
        public CardBuilder title(String t) {
            this.title = t;
            return this;
        }

        /**
         * Sets the card body content.
         * @param n body node
         * @return this builder
         */
        public CardBuilder body(Node n) {
            this.body = n;
            return this;
        }

        /**
         * Sets the card footer content.
         * @param n footer node
         * @return this builder
         */
        public CardBuilder footer(Node n) {
            this.footer = n;
            return this;
        }

        /**
         * Enables or disables shadow.
         * @param s true to show shadow
         * @return this builder
         */
        public CardBuilder shadow(boolean s) {
            this.shadow = s;
            return this;
        }

        /**
         * Enables or disables border.
         * @param b true to show border
         * @return this builder
         */
        public CardBuilder border(boolean b) {
            this.border = b;
            return this;
        }

        /**
         * Enables hover lift animation.
         * @param h true to enable hover effect
         * @return this builder
         */
        public CardBuilder hoverable(boolean h) {
            this.hoverable = h;
            return this;
        }

        /**
         * Sets padding in Tailwind units.
         * @param p padding value
         * @return this builder
         */
        public CardBuilder padding(double p) {
            this.padding = p;
            return this;
        }

        /**
         * Sets corner radius in pixels.
         * @param r radius value
         * @return this builder
         */
        public CardBuilder radius(double r) {
            this.radius = r;
            return this;
        }

        /**
         * Builds the card.
         * @return styled VBox
         */
        public VBox build() {
            VBox card = new VBox();
            TwStyle.apply(card, "bg-white", shadow ? "shadow-md" : "", "rounded-lg");
            card.setStyle("-fx-background-radius: " + radius + "px;"
                + (border ? " -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: " + radius + "px;" : ""));
            double pad = padding * 4;
            card.setPadding(new Insets(pad));
            card.setSpacing(12);

            if (title != null) {
                Label lbl = new Label(title);
                TwStyle.apply(lbl, "text-lg", "font-semibold", "text-gray-900");
                card.getChildren().add(lbl);
            }
            if (body != null) {
                VBox.setVgrow(body, Priority.ALWAYS);
                card.getChildren().add(body);
            }
            if (footer != null) {
                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);
                if (body != null) card.getChildren().add(spacer);
                TwStyle.apply(footer, "pt-3", "border-t", "border-gray-100");
                card.getChildren().add(footer);
            }

            if (hoverable) {
                FxAnimation.onHoverLift(card, -3);
            }
            return card;
        }
    }
}
