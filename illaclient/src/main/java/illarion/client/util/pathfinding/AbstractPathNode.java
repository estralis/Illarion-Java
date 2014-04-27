/*
 * This file is part of the Illarion project.
 *
 * Copyright © 2014 - Illarion e.V.
 *
 * Illarion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Illarion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package illarion.client.util.pathfinding;

import illarion.common.types.Location;

import javax.annotation.Nonnull;

/**
 * This is the shared implementation of a path node.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
abstract class AbstractPathNode implements PathNode {
    /**
     * The location of this node.
     */
    @Nonnull
    private final Location location;

    /**
     * The method of movement.
     */
    @Nonnull
    private final PathMovementMethod movementMethod;

    /**
     * Constructor that sets the location this node is assigned to.
     *
     * @param location the nodes location
     * @param movementMethod the movement method
     */
    protected AbstractPathNode(@Nonnull Location location, @Nonnull PathMovementMethod movementMethod) {
        this.location = location;
        this.movementMethod = movementMethod;
    }

    @Nonnull
    @Override
    public Location getLocation() {
        return location;
    }

    @Nonnull
    @Override
    public PathMovementMethod getMovementMethod() {
        return movementMethod;
    }
}