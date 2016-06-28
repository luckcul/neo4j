/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.raft.log.segmented;

import java.io.IOException;

import org.neo4j.coreedge.raft.log.EntryRecord;
import org.neo4j.coreedge.raft.log.LogPosition;
import org.neo4j.coreedge.raft.replication.ReplicatedContent;
import org.neo4j.coreedge.raft.state.ChannelMarshal;
import org.neo4j.cursor.CursorValue;
import org.neo4j.cursor.IOCursor;
import org.neo4j.io.fs.StoreChannel;
import org.neo4j.kernel.impl.transaction.log.ReadAheadChannel;

import static org.neo4j.coreedge.raft.log.EntryRecord.read;

/**
 * A cursor for iterating over RAFT log entries starting at an index and until the end of the segment is met.
 * The segment is demarcated by the ReadAheadChannel provided, which should properly signal the end of the channel.
 */
class EntryRecordCursor implements IOCursor<EntryRecord>
{
    private ReadAheadChannel<StoreChannel> bufferedReader;

    private final LogPosition position;
    private final CursorValue<EntryRecord> currentRecord = new CursorValue<>();
    private ChannelMarshal<ReplicatedContent> contentMarshal;

    EntryRecordCursor( ReadAheadChannel<StoreChannel> bufferedReader,
            ChannelMarshal<ReplicatedContent> contentMarshal, long startIndex ) throws IOException
    {
        this.bufferedReader = bufferedReader;
        this.contentMarshal = contentMarshal;
        position = new LogPosition( startIndex, bufferedReader.position() );
    }

    @Override
    public boolean next() throws IOException
    {
        EntryRecord entryRecord = read( bufferedReader, contentMarshal );
        if ( entryRecord != null )
        {
            currentRecord.set( entryRecord );
            position.byteOffset = bufferedReader.position();
            position.logIndex++;
            return true;
        }
        else
        {
            currentRecord.invalidate();
            return false;
        }
    }

    @Override
    public void close() throws IOException
    {
        // the cursor does not own any resources, the channel is owned by the pooled Reader
        bufferedReader = null;
    }

    @Override
    public EntryRecord get()
    {
        return currentRecord.get();
    }

    public LogPosition position()
    {
        return position;
    }
}
