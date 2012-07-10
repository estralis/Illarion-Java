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
package illarion.client.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.EffectBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import illarion.client.net.server.events.BroadcastInformReceivedEvent;
import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

/**
 * The task of this handler is to accept and display broadcast informs.
 *
 * @author Martin Karing &gt;nitram@illarion.org&lt;
 */
public final class BroadcastInformHandler implements EventSubscriber<BroadcastInformReceivedEvent>, ScreenController {
    /**
     * The logger that is used for the logging output of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(BroadcastInformHandler.class);

    /**
     * This is a reference to the panel that is supposed to be used as container of the server inform messages.
     */
    private Element parentPanel;

    /**
     * The inform handler that is supposed to display the inform messages generated by this class.
     */
    private final InformHandler informHandler;

    /**
     * Default constructor that prepares the structures needed for this handler to work properly.
     *
     * @param parentHandler the handler that is supposed to display the messages generated in this class
     */
    public BroadcastInformHandler(final InformHandler parentHandler) {
        informHandler = parentHandler;
    }

    @Override
    public void bind(final Nifty nifty, final Screen screen) {
        parentPanel = screen.findElementByName("broadcastMsgPanel");
    }

    @Override
    public void onEndScreen() {
        EventBus.unsubscribe(BroadcastInformReceivedEvent.class, this);
    }

    @Override
    public void onEvent(final BroadcastInformReceivedEvent event) {
        if (parentPanel == null) {
            LOGGER.warn("Received server inform before the GUI became ready.");
            return;
        }

        final LabelBuilder labelBuilder = new LabelBuilder();
        labelBuilder.label(event.getMessage());
        labelBuilder.font("menuFont");
        labelBuilder.invisibleToMouse();

        final EffectBuilder effectBuilder = new EffectBuilder("hide");
        effectBuilder.startDelay(10000 + (event.getMessage().length() * 50));
        labelBuilder.onHideEffect(effectBuilder);

        informHandler.showInform(labelBuilder, parentPanel);
    }

    @Override
    public void onStartScreen() {
        EventBus.subscribe(BroadcastInformReceivedEvent.class, this);
    }
}
