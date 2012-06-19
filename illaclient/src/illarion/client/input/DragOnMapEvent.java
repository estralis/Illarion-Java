/*
 * This file is part of the Illarion Client.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Client.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.client.input;

import de.lessvoid.nifty.slick2d.input.ForwardingInputSystem;

/**
 * This event is published in case a dragging operation on the map was noted.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class DragOnMapEvent {
    /**
     * The X coordinate on the screen where the dragging operation started.
     */
    private final int oldX;

    /**
     * The Y coordinate on the screen where the dragging operation started.
     */
    private final int oldY;

    /**
     * The X coordinate on the screen where the dragging operation is currently.
     */
    private final int newX;

    /**
     * The Y coordinate on the screen where the dragging operation is currently.
     */
    private final int newY;

    /**
     * The key that is used for the dragging operation.
     */
    private final int activeKey;

    /**
     * The controls used to override the default forwarding behaviour of the Slick renderer.
     */
    private final ForwardingInputSystem forwardingControl;

    /**
     * Create and initialize such a event.
     *
     * @param startX                 the X coordinate where the dragging starts
     * @param startY                 the Y coordinate where the dragging starts
     * @param stopX                  the X coordinate where the dragging is currently
     * @param stopY                  the Y coordinate where the dragging is currently
     * @param pressedKey             the key used for the dragging operation
     * @param inputForwardingControl the control class to change the forwarding behaviour
     */
    public DragOnMapEvent(final int startX, final int startY, final int stopX, final int stopY, final int pressedKey,
                          final ForwardingInputSystem inputForwardingControl) {
        oldX = startX;
        oldY = startY;
        newX = stopX;
        newY = stopY;
        forwardingControl = inputForwardingControl;
        activeKey = pressedKey;
    }

    /**
     * Get the X coordinate of the location there the mouse was located before the dragging operation.
     *
     * @return the X coordinate of the starting location
     */
    public int getOldX() {
        return oldX;
    }

    /**
     * Get the Y coordinate of the location there the mouse was located before the dragging operation.
     *
     * @return the Y coordinate of the starting location
     */
    public int getOldY() {
        return oldY;
    }

    /**
     * Get the X coordinate of the location there the mouse was located after the dragging operation.
     *
     * @return the X coordinate of the current location
     */
    public int getNewX() {
        return newX;
    }

    /**
     * Get the Y coordinate of the location there the mouse was located after the dragging operation.
     *
     * @return the Y coordinate of the current location
     */
    public int getNewY() {
        return newY;
    }

    /**
     * Get the input forwarding control that apply to the input event source this event originates from.
     *
     * @return the forwarding control
     */
    public ForwardingInputSystem getForwardingControl() {
        return forwardingControl;
    }

    /**
     * Get the key used for this dragging operation.
     *
     * @return the key used for the dragging operation
     */
    public int getKey() {
        return activeKey;
    }
}
