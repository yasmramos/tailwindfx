package io.github.yasmramos.tailwindfx.components;

import io.github.yasmramos.tailwindfx.TailwindFX;
import io.github.yasmramos.tailwindfx.animation.TwAnimation;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * TwCard — Pre-styled card container component.
 * 
 * Uses base .card class from tailwindfx-components.css with utility modifiers.
 * 
 * <pre>
 * TwCard card = TwCard.create()
 *     .title("Revenue")
 *     .body(chart)
 *     .footer(actions)
 *     .build();
 * 
 * TwCard simpleCard = TwCard.simple(content);
 * </pre>
 */
public class TwCard extends VBox {

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
     * @return styled TwCard
     */
    public static TwCard simple(Node content) {
        return create().body(content).build();
    }

    /**
     * Creates a card with title and content.
     * @param title card title
     * @param content card content
     * @return styled TwCard
     */
    public static TwCard withTitle(String title, Node content) {
        return create().title(title).body(content).build();
    }

    /**
     * Protected constructor for builder usage.
     */
    protected TwCard() {
        super();
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
        private String size = "md"; // sm, md, lg
        private String radius = "lg"; // sm, md, lg, xl

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
         * Sets card size (sm, md, lg).
         * @param s size value
         * @return this builder
         */
        public CardBuilder size(String s) {
            this.size = s;
            return this;
        }

        /**
         * Sets corner radius (sm, md, lg, xl).
         * @param r radius value
         * @return this builder
         */
        public CardBuilder radius(String r) {
            this.radius = r;
            return this;
        }

        /**
         * Builds the card.
         * @return styled TwCard
         */
        public TwCard build() {
            TwCard card = new TwCard();
            
            // Apply base card class and variants
            TailwindFX.apply(card, 
                "card",
                shadow ? "shadow-md" : "",
                border ? "border" : "",
                hoverable ? "card-hoverable" : "",
                "card-" + size,
                "rounded-" + radius
            );
            
            card.setSpacing(12);

            if (title != null) {
                Label lbl = new Label(title);
                TailwindFX.apply(lbl, "text-lg", "font-semibold", "text-gray-900");
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
                TailwindFX.apply(footer, "pt-3", "border-t", "border-gray-100");
                card.getChildren().add(footer);
            }

            if (hoverable) {
                TwAnimation.onHoverLift(card, -3);
            }
            return card;
        }
    }
}
