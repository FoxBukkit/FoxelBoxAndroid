/**
 * This file is part of FoxBukkitChatLink.
 *
 * FoxBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.app.json;

import android.text.Spannable;
import com.foxelbox.app.gui.ChatFormatterUtility;

public class MessageContents {
    public MessageContents(String plain, String xml) {
        this.plain = plain;
        this.xml = xml;
    }

    public final String plain;
    public final String xml;

    private transient Spannable formatted = null;
    public Spannable getFormatted() {
        if(plain == null)
            return null;
        if(formatted == null)
            formatted = ChatFormatterUtility.formatString(plain);
        return formatted;
    }
}
