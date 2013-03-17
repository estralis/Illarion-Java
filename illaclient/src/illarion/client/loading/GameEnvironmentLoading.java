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
package illarion.client.loading;

import illarion.client.world.World;
import illarion.common.util.ProgressMonitor;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * This loading task takes care for loading the components of the game environment that still need to be loaded.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
@NotThreadSafe
public final class GameEnvironmentLoading implements LoadingTask {
    /**
     * This is set {@code true} once the loading of the game components is done.
     */
    private boolean loadingDone;

    @Nonnull
    private final ProgressMonitor monitor = new ProgressMonitor();

    @Override
    public void load() {
        World.initWorldComponents();
        loadingDone = true;
        monitor.setProgress(1.f);
    }

    @Override
    public boolean isLoadingDone() {
        return loadingDone;
    }

    @Nonnull
    @Override
    public ProgressMonitor getProgressMonitor() {
        return monitor;
    }
}
