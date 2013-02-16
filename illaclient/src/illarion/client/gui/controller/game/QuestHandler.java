/*
 * This file is part of the Illarion Client.
 *
 * Copyright © 2013 - Illarion e.V.
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
package illarion.client.gui.controller.game;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.builder.ElementBuilder;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import illarion.client.IllaClient;
import illarion.client.graphics.FontLoader;
import illarion.client.gui.QuestGui;
import illarion.client.input.InputReceiver;
import illarion.client.util.UpdateTask;
import illarion.client.world.World;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class takes care for managing the quest log.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class QuestHandler implements QuestGui, ScreenController {
    /**
     * This is a single quest that is shown in the quest log.
     */
    private static final class QuestEntry implements Comparable<QuestEntry> {
        /**
         * The ID of the quest.
         */
        private final int questId;

        /**
         * The displayed name of the quest.
         */
        private String name;

        /**
         * The description of the current quest state.
         */
        private String description;

        /**
         * The flag if this quest is now finished or not.
         */
        private boolean finished;

        /**
         * The constructor of the quest.
         *
         * @param questId     the ID of the quest
         * @param name        the name of the quest
         * @param description the description of the quest state
         * @param finished    {@code true} in case the quest is finished
         */
        QuestEntry(final int questId, final String name, final String description, final boolean finished) {
            this.questId = questId;
            updateData(name, description, finished);
        }

        /**
         * Update the data of this quest.
         *
         * @param name        the name of the quest
         * @param description the description of the quest state
         * @param finished    {@code true} in case the quest is finished
         */
        void updateData(final String name, final String description, final boolean finished) {
            this.name = name;
            this.description = description;
            this.finished = finished;
        }

        @Override
        public int compareTo(final QuestEntry o) {
            if (o.finished && !finished) {
                return -1;
            }
            if (!o.finished && finished) {
                return 1;
            }

            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(final Object obj) {
            if (super.equals(obj)) {
                return true;
            }
            if (obj instanceof QuestEntry) {
                return questId == ((QuestEntry) obj).questId;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return questId;
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * Get the description of the quest.
         *
         * @return the quest description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get the name of the quest.
         *
         * @return the name of the quest
         */
        public String getName() {
            return name;
        }

        /**
         * Get the ID of the quest.
         *
         * @return the ID of the quest
         */
        public int getQuestId() {
            return questId;
        }

        /**
         * Check if the quest is finished.
         *
         * @return {@code true} in case the quest is finished
         */
        public boolean isFinished() {
            return finished;
        }
    }

    /**
     * The window that shows the quest log.
     */
    private Window questWindow;

    /**
     * This is the list of quests that are currently not shown in the GUI.
     */
    private final List<QuestEntry> hiddenList;

    /**
     * In case this is {@code true} even the finished quests are shown.
     */
    private boolean showFinishedQuests;

    /**
     * The reference to the Nifty instance this handler is bound to.
     */
    private Nifty nifty;

    /**
     * The reference to the screen this handler is bound to.
     */
    private Screen screen;

    /**
     * Default constructor.
     */
    public QuestHandler() {
        hiddenList = new ArrayList<QuestEntry>();
    }

    /**
     * The event subscriber for input events.
     *
     * @param topic the event topic
     * @param data  the event data
     */
    @EventTopicSubscriber(topic = InputReceiver.EB_TOPIC)
    public void onInputEvent(final String topic, @Nonnull final String data) {
        if ("ToggleQuest".equals(data)) {
            if (isQuestLogVisible()) {
                hideQuestLog();
            } else {
                showQuestLog();
            }
        }
    }

    @Override
    public boolean isQuestLogVisible() {
        return questWindow.getElement().isVisible();
    }

    @Override
    public void hideQuestLog() {
        questWindow.closeWindow();
    }

    @Override
    public void showQuestLog() {
        questWindow.getElement().show(new EndNotify() {
            @Override
            public void perform() {
                questWindow.moveToFront();
            }
        });
    }

    /**
     * The event subscriber for click events on the quest button.
     *
     * @param topic the event topic
     * @param data  the event data
     */
    @NiftyEventSubscriber(id = "openQuestBtn")
    public void onQuestLogButtonClicked(final String topic, final ButtonClickedEvent data) {
        if (isQuestLogVisible()) {
            hideQuestLog();
        } else {
            showQuestLog();
        }
    }

    /**
     * Event Handler for change events of the selection int he quest list box.
     *
     * @param topic the event topic
     * @param event the event data
     */
    @NiftyEventSubscriber(id = "questLog#questList")
    public void onSelectedQuestChanged(@Nonnull final String topic,
                                       @Nonnull final ListBoxSelectionChangedEvent<QuestEntry> event) {
        final Element descriptionArea = getDescriptionArea();
        for (final Element oldChildren : descriptionArea.getElements()) {
            oldChildren.markForRemoval();
        }

        final List<QuestEntry> selectedEntries = event.getSelection();
        if ((selectedEntries == null) || selectedEntries.isEmpty()) {
            return;
        }

        final QuestEntry selectedEntry = selectedEntries.get(0);

        final LabelBuilder titleLabel = new LabelBuilder();
        titleLabel.label(selectedEntry.getName());
        titleLabel.font(FontLoader.Fonts.Menu.getName());
        titleLabel.marginLeft("5px");
        titleLabel.marginRight("5px");
        titleLabel.marginBottom("10px");
        titleLabel.width((descriptionArea.getWidth() - 10) + "px");
        titleLabel.wrap(true);
        titleLabel.build(nifty, screen, descriptionArea);

        if (!selectedEntry.getDescription().isEmpty()) {
            final LabelBuilder descriptionLabel = new LabelBuilder();
            descriptionLabel.label(selectedEntry.getDescription());
            descriptionLabel.font(FontLoader.Fonts.Text.getName());
            descriptionLabel.marginLeft("5px");
            descriptionLabel.marginRight("5px");
            descriptionLabel.width((descriptionArea.getWidth() - 10) + "px");
            descriptionLabel.textHAlign(ElementBuilder.Align.Left);
            descriptionLabel.wrap(true);
            descriptionLabel.build(nifty, screen, descriptionArea);
        }

        if (selectedEntry.isFinished()) {
            final LabelBuilder finishedLabel = new LabelBuilder();
            finishedLabel.label("${gamescreen-bundle.questFinished}");
            finishedLabel.font(FontLoader.Fonts.Text.getName());
            finishedLabel.marginLeft("5px");
            finishedLabel.marginRight("5px");
            finishedLabel.marginTop("15px");
            finishedLabel.width((descriptionArea.getWidth() - 10) + "px");
            finishedLabel.textHAlign(ElementBuilder.Align.Center);
            finishedLabel.wrap(true);
            finishedLabel.build(nifty, screen, descriptionArea);
        }
    }

    /**
     * Get the GUI area that contains the description.
     *
     * @return the element of the description area
     */
    private Element getDescriptionArea() {
        return questWindow.getElement().findElementByName("#questDescription");
    }

    /**
     * Event Handler for change events of the "Show finished quests" checkbox.
     *
     * @param topic the event topic
     * @param event the event data
     */
    @NiftyEventSubscriber(id = "questLog#showFinishedCheckbox")
    public void onShowFinishedChange(@Nonnull final String topic, @Nonnull final CheckBoxStateChangedEvent event) {
        IllaClient.getCfg().set("questShowFinished", event.isChecked());
        showFinishedQuests = event.isChecked();

        if (showFinishedQuests) {
            for (final QuestEntry hiddenEntry : hiddenList) {
                insertToGuiList(hiddenEntry);
            }
            hiddenList.clear();
        } else {
            for (final QuestEntry visibleEntry : getQuestList().getItems()) {
                if (visibleEntry.isFinished() && !hiddenList.contains(visibleEntry)) {
                    hiddenList.add(visibleEntry);
                }
            }
            getQuestList().removeAllItems(hiddenList);
        }
    }

    @Override
    public void bind(final Nifty nifty, final Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

        questWindow = screen.findNiftyControl("questLog", Window.class);
        final Element questWindowElement = questWindow.getElement();
        questWindowElement.setConstraintX(new SizeValue(IllaClient.getCfg().getString("questWindowPosX")));
        questWindowElement.setConstraintY(new SizeValue(IllaClient.getCfg().getString("questWindowPosY")));

        final CheckBox showFinished = questWindowElement.findNiftyControl("#showFinishedCheckbox", CheckBox.class);
        showFinished.setChecked(IllaClient.getCfg().getBoolean("questShowFinished"));
        showFinishedQuests = showFinished.isChecked();
    }

    @Override
    public void onEndScreen() {
        nifty.unsubscribeAnnotations(this);
        AnnotationProcessor.unprocess(this);

        IllaClient.getCfg().set("questWindowPosX", questWindow.getElement().getX() + "px");
        IllaClient.getCfg().set("questWindowPosY", questWindow.getElement().getY() + "px");
    }

    @Override
    public void onStartScreen() {
        nifty.subscribeAnnotations(this);
        AnnotationProcessor.process(this);
    }

    @Override
    public void setDisplayedQuest(final int questId) {
        final ListBox<QuestEntry> guiList = getQuestList();
        for (final QuestEntry guiListEntry : guiList.getItems()) {
            if (guiListEntry.getQuestId() == questId) {
                guiList.selectItem(guiListEntry);
            }
        }
    }

    /**
     * Fetch the reference to the quest list.
     *
     * @return the quest list
     */
    private ListBox<QuestEntry> getQuestList() {
        //noinspection unchecked
        return (ListBox<QuestEntry>) questWindow.getElement().findNiftyControl("#questList", ListBox.class);
    }

    @Override
    public void setQuest(final int questId, @Nonnull final String name, @Nonnull final String description, final boolean finished) {
        World.getUpdateTaskManager().addTask(new UpdateTask() {
            @Override
            public void onUpdateGame(@Nonnull final GameContainer container, final StateBasedGame game,
                                     final int delta) {
                setQuestInternal(questId, name, description, finished);
            }
        });
    }

    /**
     * The internal method to set the quest. This needs to be called during the update call before Nifty itself is
     * updated.
     *
     * @param questId     the ID of the quest
     * @param name        the name of the quest
     * @param description the current description of the quest
     * @param finished    {@code true} if the quest is finished
     */
    private void setQuestInternal(final int questId, @Nonnull final String name, @Nonnull final String description,
                                  final boolean finished) {
        final QuestEntry oldEntry = findQuest(questId);
        if (oldEntry == null) {
            final QuestEntry newEntry = new QuestEntry(questId, name, description, finished);
            if (!finished || showFinishedQuests) {
                insertToGuiList(newEntry);
            } else {
                hiddenList.add(newEntry);
            }
        } else {
            final boolean changeOrder = (oldEntry.isFinished() != finished) || !oldEntry.getName().equals(name);
            final boolean wasFinished = oldEntry.isFinished();
            oldEntry.updateData(name, description, finished);
            if (changeOrder) {
                if (!wasFinished || showFinishedQuests) {
                    getQuestList().removeItem(oldEntry);
                }
                if (finished || showFinishedQuests) {
                    if (wasFinished) {
                        hiddenList.remove(oldEntry);
                    }
                    insertToGuiList(oldEntry);
                }
            }
        }
    }

    /**
     * Find a existing instance of the quest object with the ID supplied. This function searches both the GUI and the
     * hidden list.
     *
     * @param questId the ID of the quest to search
     * @return the quest instance or {@code null} in case its not found
     */
    @Nullable
    private QuestEntry findQuest(final int questId) {
        for (final QuestEntry hiddenListEntry : hiddenList) {
            if (hiddenListEntry.getQuestId() == questId) {
                return hiddenListEntry;
            }
        }

        for (final QuestEntry guiListEntry : getQuestList().getItems()) {
            if (guiListEntry.getQuestId() == questId) {
                return guiListEntry;
            }
        }

        return null;
    }

    /**
     * This function is used to insert a quest into the GUI list. This takes  are to apply the required order to the
     * quest.
     *
     * @param entry the entry to add
     */
    private void insertToGuiList(final QuestEntry entry) {
        final ListBox<QuestEntry> guiList = getQuestList();
        final List<QuestEntry> questEntries = guiList.getItems();
        int currentStart = 0;
        int currentEnd = questEntries.size() - 1;

        while (currentStart <= currentEnd) {
            final int middle = currentStart + ((currentEnd - currentStart) >> 1);
            final QuestEntry foundItem = questEntries.get(middle);
            final int compareResult = foundItem.compareTo(entry);

            if (compareResult < 0) {
                currentStart = middle + 1;
            } else if (compareResult > 0) {
                currentEnd = middle - 1;
            } else {
                guiList.insertItem(entry, middle);
                return;
            }
        }

        guiList.insertItem(entry, currentStart);
    }
}