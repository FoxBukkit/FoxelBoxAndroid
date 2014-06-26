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
package com.foxelbox.app.json.chat;

import java.util.UUID;

public class ChatMessageOut {
    public ChatMessageOut(String server, UserInfo from, String plain, String xml) {
        this.server = server;
        this.from = from;
        this.to = new MessageTarget("all", null);
        this.contents = new MessageContents(plain, xml);
        this.context = UUID.randomUUID();
    }

    public final String server;
    public final UserInfo from;
    public final MessageTarget to;

    public final long timestamp = System.currentTimeMillis() / 1000;

    public final UUID context;
    public final boolean finalize_context = false;
    public final String type = "text";

    public final MessageContents contents;
}