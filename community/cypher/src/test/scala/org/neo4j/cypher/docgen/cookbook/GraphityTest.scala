/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.docgen.cookbook

import org.junit.Test
import org.junit.Assert._
import org.neo4j.cypher.docgen.DocumentingTestBase
import org.junit.Ignore

class GraphityTest extends DocumentingTestBase {
  def graphDescription = List(
    "Joe has Joe_s1",
    "Joe_s1 next Joe_s2",
    "Bill has Bill_s1",
    "Bill_s1 next Bill_s2",
    "Ted has Ted_s1",
    "Ted_s1 next Ted_s2",
    "Bob bob_knows Ted",
    "Bob has Bob_s1",
    "Ted bob_knows Bill",
    "Jane jane_knows Bill",
    "Bill jane_knows Joe",
    "Joe jane_knows Bob")

  def section = "cookbook"

  @Test def peopleSimilarityTags() {
    testQuery(
      title = "Find Activity Streams in a network without scaling penalty",
      text = """This is an approach put forward by Rene Pickard as http://www.rene-pickhardt.de/graphity-an-efficient-graph-model-for-retrieving-the-top-k-news-feeds-for-users-in-social-networks/[Graphity].
        In short, a linked list is created for every persons friends in the order that the last activities of these friends have occured.
        To find the activity stream for a person, the friend just follow the linked list of the friend list, and retrieve the needed amount of activities form the respective activity list of the friends.""",
      queryText = "START me=node:node_auto_index(name = \"Jane\") " +
        "MATCH p=me-[:jane_knows*]->friend, " +
        "friend-[:has]->status " +
        "RETURN me.name, friend.name, status.name, length(p) " +
        "ORDER BY length(p)",
      returns = "The activity stream for Jane.",
      (p) => assertEquals(List(Map("status.name" -> "Bill_s1", "friend.name" -> "Bill", "me.name" -> "Jane", "LENGTH(p)" -> 1), 
          Map("status.name" -> "Joe_s1", "friend.name" -> "Joe", "me.name" -> "Jane", "LENGTH(p)" -> 2),
          Map("status.name" -> "Bob_s1", "friend.name" -> "Bob", "me.name" -> "Jane", "LENGTH(p)" -> 3)
          ), p.toList))
  }
}
