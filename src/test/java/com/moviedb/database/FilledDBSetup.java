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

package com.moviedb.database;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;


/**
 * This class provides a pre-filled H2 database setup for testing purposes.
 */
public abstract class FilledDBSetup extends EmptyDBSetup {

    /**
     * Initializes the test database with pre-defined data.
     */
    @BeforeEach
    @Override
    public void setUpDB() {
        super.setUpDB();  // Fill the database first as empty

        // Actor IDs are auto-generated by the database
        insertActor("Robert De Niro");      // id: 1
        insertActor("Meryl Streep");        // id: 2
        insertActor("Jamie Foxx");          // id: 3
        insertActor("Christoph Waltz");     // id: 4
        insertActor("Margot Robbie");       // id: 5
        insertActor("Leonardo Di Caprio");  // id: 6

        // Actor IDs are auto-generated by the database
        int inceptionId = insertMovie("Inception",                      // id: 1
                2010, "Christopher Nolan");
        int wolfWallStreetId = insertMovie("The Wolf of Wall Street",   // id: 2
                2013, "Martin Scorsese");
        int djangoId = insertMovie("Django Unchained",                  // id: 3
                2012, "Quentin Tarantino");
        int deerHunterId = insertMovie("The Deer Hunter",               // id: 4
                1978, "Michael Cimino");
        int taxiDriverId = insertMovie("Taxi Driver",                   // id: 5
                1976, "Martin Scorsese");

        // Create movie-actor associations using a helper method
        linkMovieActor(inceptionId, "Leonardo Di Caprio");
        linkMovieActor(wolfWallStreetId, "Leonardo Di Caprio");
        linkMovieActor(wolfWallStreetId, "Margot Robbie");
        linkMovieActor(djangoId, "Jamie Foxx");
        linkMovieActor(djangoId, "Christoph Waltz");
        linkMovieActor(deerHunterId, "Robert De Niro");
        linkMovieActor(deerHunterId, "Meryl Streep");
        linkMovieActor(taxiDriverId, "Robert De Niro");

        // Create movie-genre associations using a helper method
        linkMovieGenre(inceptionId, "Adventure");
        linkMovieGenre(inceptionId, "Fantasy");
        linkMovieGenre(wolfWallStreetId, "Comedy");
        linkMovieGenre(wolfWallStreetId, "Drama");
        linkMovieGenre(djangoId, "Action");
        linkMovieGenre(djangoId, "Adventure");
        linkMovieGenre(deerHunterId, "Drama");
        linkMovieGenre(taxiDriverId, "Crime");
        linkMovieGenre(taxiDriverId, "Drama");
    }


    /**
     * Inserts an actor into the actors table and returns the generated actor ID.
     *
     * @param name The name of the actor to insert.
     * @return The generated ID for the inserted actor, or -1 if the insertion failed.
     */
    private int insertActor(String name) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO actors (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
            fail("Error inserting actor: " + name);
        }
        return -1;
    }


    /**
     * Inserts a movie into the movies table and returns the generated movie ID.
     *
     * @param title       The title of the movie to insert.
     * @param releaseYear The release year of the movie.
     * @param director    The director of the movie.
     * @return The generated ID for the inserted movie, or -1 if the insertion failed.
     */
    private int insertMovie(String title, int releaseYear, String director) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO movies (title, release_year, director) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, releaseYear);
            pstmt.setString(3, director);
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
            fail("Error inserting movie: " + title);
        }
        return -1;
    }


    /**
     * Links a movie and an actor by their names in the movie_actors table.
     * Retrieves the actor's ID based on the given name and inserts a new
     * record into the movie_actors table with the movie ID and actor ID.
     *
     * @param movieId   The ID of the movie.
     * @param actorName The name of the actor to link to the movie.
     */
    private void linkMovieActor(int movieId, String actorName) {
        // Find actor ID
        int actorId = getActorIdByName(actorName);
        if (actorId != -1) {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO movie_actors (movie_id, actor_id) VALUES (?, ?)")) {
                pstmt.setInt(1, movieId);
                pstmt.setInt(2, actorId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                System.out.println("Message: " + e.getMessage());
                fail("Error linking movie " + movieId + " with actor " + actorName);
            }
        } else {
            fail("Actor not found: " + actorName);
        }
    }


    /**
     * Retrieves the ID of an actor from the actors table based on the actor's name.
     *
     * @param actorName The name of the actor whose ID to retrieve.
     * @return The ID of the actor, or -1 if the actor could not be found.
     */
    private int getActorIdByName(String actorName) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id FROM actors WHERE name = ?")) {
            pstmt.setString(1, actorName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
            fail("Error retrieving actor ID for: " + actorName);
        }
        return -1; // If actor not found
    }


    /**
     * Links a movie and a genre by their names in the movie_genres table.
     * Retrieves the genre's ID based on the given name and inserts a new
     * record into the movie_genres table with the movie ID and genre ID.
     *
     * @param movieId   The ID of the movie.
     * @param genreName The name of the genre to link to the movie.
     */
    private void linkMovieGenre(int movieId, String genreName) {
        // Find genre ID
        int genreId = getGenreIdByName(genreName);
        if (genreId != -1) {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)")) {
                pstmt.setInt(1, movieId);
                pstmt.setInt(2, genreId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                System.out.println("Message: " + e.getMessage());
                fail("Error linking movie " + movieId + " with genre " + genreName);
            }
        } else {
            fail("Genre not found: " + genreName);
        }
    }


    /**
     * Retrieves the ID of a genre from the genres table based on the genre's name.
     *
     * @param genreName The name of the genre which ID to retrieve.
     * @return The ID of the genre, or -1 if the genre could not be found.
     */
    private int getGenreIdByName(String genreName) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT id FROM genres WHERE name = ?")) {
            pstmt.setString(1, genreName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
            fail("Error retrieving genre ID for: " + genreName);
        }
        return -1;
    }
}
