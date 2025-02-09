/*
 * Copyright (c) 2023-2024 Aaro Koinsaari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.moviedb.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.moviedb.database.FilledDBSetup;
import com.moviedb.models.Actor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * This class contains unit tests for the ActorDao class using a pre-filled H2 database setup.
 */
public class ActorDaoFilledDBTest extends FilledDBSetup {

    private ActorDao dao;


    /**
     * Additional setup for the filled database for each test.
     * Initializes the connection to the test database for each test.
     */
    @BeforeEach
    void setUp() {
        dao = new ActorDao(connection);
    }


    @Test
    @DisplayName("Create a new actor in the database and verify")
    void testCreate() {
        String name = "New Actor";
        Actor newActor = new Actor(name);

        // Insert new actor to database and fetch it
        int actorId = assertDoesNotThrow(() -> dao.create(newActor),
                "Creating a new actor should not throw SQLException");

        Optional<Actor> fetchedActor = assertDoesNotThrow(() -> dao.read(actorId),
                "Reading the created actor should not throw SQLException");

        assertTrue(fetchedActor.isPresent(), "Actor should be present");
        fetchedActor.ifPresent(actor -> {
            assertEquals(name, actor.getName(), "Actor's name should match");
            assertEquals(actorId, actor.getId(), "Actor's ID should match");
        });
    }


    @Test
    @DisplayName("Read an existing actor in the database")
    void testReadExistingActor() {
        Optional<Actor> fetchedExistingActor = assertDoesNotThrow(() -> dao.read(6),
                "Reading an existing actor should not throw SQLException");
        assertTrue(fetchedExistingActor.isPresent(), "Actor should be present");
        fetchedExistingActor.ifPresent(actor -> {
            assertEquals("Leonardo Di Caprio", actor.getName(), "Actor name should match");
            assertEquals(6, actor.getId(), "Actor ID should match");
        });
    }


    @Test
    @DisplayName("Read a non-existing actor from the database")
    void testReadNonExistingActor() {
        Optional<Actor> fetchedNonExistentActor = assertDoesNotThrow(() -> dao.read(99),
                "Reading a non-existent actor should not throw SQLException");
        assertTrue(fetchedNonExistentActor.isEmpty(), "Actor should not be present");
    }


    @Test
    @DisplayName("Read a newly inserted actor from the database")
    void testReadNewInsertedActor() {
        String name = "Test Actor 1";
        Actor testActor = new Actor(name);
        int actorId = assertDoesNotThrow(() -> dao.create(testActor),
                "Creating new actor should not throw SQLException");

        Optional<Actor> foundActor = assertDoesNotThrow(() -> dao.read(actorId),
                "Reading the newly created actor should not throw SQLException");

        assertTrue(foundActor.isPresent(), "Actor should be present");
        foundActor.ifPresent(actor -> {
            assertEquals(name, actor.getName(), "Actor name should match");
            assertEquals(actorId, actor.getId(), "Actor ID should match");
        });
    }


    @Test
    @DisplayName("Update an existing actor in the database")
    void testUpdate() {
        int actorId = 2;  // Meryl Streep
        String updatedName = "Test Name";

        Optional<Actor> actorOptional = assertDoesNotThrow(() -> dao.read(actorId),
                "Reading an existing actor should not throw SQLException");
        assertTrue(actorOptional.isPresent(), "Actor should be in the database");

        Actor actorToUpdate = actorOptional.get();
        actorToUpdate.setName(updatedName);

        boolean updateSuccessful = assertDoesNotThrow(() -> dao.update(actorToUpdate),
                "Updating the actor should not throw SQLException");
        assertTrue(updateSuccessful, "Actor update should be successful");

        Optional<Actor> updatedOptionalActor = assertDoesNotThrow(() -> dao.read(actorId),
                "Reading the updated actor should not throw SQLException");
        assertTrue(updatedOptionalActor.isPresent(), "Updated actor should still be in the database");
        Actor updatedActor = updatedOptionalActor.get();
        assertEquals(updatedName, updatedActor.getName(), "Actor's name should be updated in the database");
    }
}
