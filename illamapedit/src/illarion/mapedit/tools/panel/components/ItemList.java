/*
 * This file is part of the Illarion Mapeditor.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Mapeditor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Mapeditor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Mapeditor.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.mapedit.tools.panel.components;

import illarion.mapedit.resource.ItemImg;
import illarion.mapedit.resource.loaders.ItemLoader;
import illarion.mapedit.tools.panel.cellrenderer.ItemImgCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ItemList extends JPanel {

    public interface ItemSelectedListener {
        void selectedItem(ItemImg img);
    }

    private final JList<ItemImg> itemList;

    public ItemList(final ItemList.ItemSelectedListener itemSelectedListener) {

        itemList = new JList<ItemImg>(ItemLoader.getInstance().getTiles());
        itemList.setCellRenderer(new ItemImgCellRenderer());
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                itemSelectedListener.selectedItem(itemList.getSelectedValue());
            }
        });
        final JScrollPane scrollPane = new JScrollPane(itemList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);
    }
}
