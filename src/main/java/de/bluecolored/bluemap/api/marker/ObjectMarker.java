/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.api.marker;

import com.flowpowered.math.vector.Vector3d;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public abstract class ObjectMarker extends DistanceRangedMarker {

    private String detail;

    @Nullable
    private String link;
    private boolean newTab;

    public ObjectMarker(String type, String label, Vector3d position) {
        super(type, label, position);
        this.detail = Objects.requireNonNull(label, "label must not be null");
        this.link = null;
        this.newTab = false;
    }

    /**
     * Getter for the detail of this marker. The label can include html-tags.
     * @return the detail of this {@link ObjectMarker}
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Sets the detail of this {@link ObjectMarker}. The detail can include html-tags.<br>
     * This is the text that will be displayed on the popup when you click on this marker.
     * <p>
     * 	<b>Important:</b><br>
     * 	Html-tags in the label will not be escaped, so you can use them to style the {@link ObjectMarker}-detail.<br>
     * 	Make sure you escape all html-tags from possible user inputs to prevent possible <a href="https://en.wikipedia.org/wiki/Cross-site_scripting">XSS-Attacks</a> on the web-client!
     * </p>
     *
     * @param detail the new detail for this {@link ObjectMarker}
     */
    public void setDetail(String detail) {
        this.detail = Objects.requireNonNull(detail);
    }

    /**
     * Gets the link-address of this {@link Marker}.<br>
     * If a link is present, this link will be followed when the user clicks on the marker in the web-app.
     *
     * @return the {@link Optional} link
     */
    public Optional<String> getLink() {
        return Optional.ofNullable(link);
    }

    /**
     * If this is <code>true</code> the link ({@link #getLink()}) will be opened in a new tab.
     * @return whether the link will be opened in a new tab
     * @see #getLink()
     */
    public boolean isNewTab() {
        return newTab;
    }

    /**
     * Sets the link-address of this {@link Marker}.<br>
     * If a link is present, this link will be followed when the user clicks on the marker in the web-app.
     *
     * @param link the link, or <code>null</code> to disable the link
     * @param newTab whether the link should be opened in a new tab
     */
    public void setLink(String link, boolean newTab) {
        this.link = Objects.requireNonNull(link, "link must not be null");
        this.newTab = newTab;
    }

    /**
     * Removes the link of this {@link Marker}.
     */
    public void removeLink() {
        this.link = null;
        this.newTab = false;
    }

}
