package com.bdang.storage;

import com.bdang.facts.Fact;
import com.bdang.storage.exception.UnregisteredConceptException;

import java.util.List;

/**
 * Accessor for animal facts
 */
public interface Accessor {
    /**
     * Write the given fact. This is an idempotent method.
     * @param fact Fact to be written
     * @return Fact ID
     */
    String put(Fact fact);

    /**
     * Delete a fact with the given ID.
     * @param id Fact ID to be deleted
     * @return True if deleted
     */
    boolean delete(String id);

    /**
     * Retrieve the fact with the given ID.
     * @param id Fact ID to be retrieved
     * @return Fact
     */
    Fact get(String id);

    /**
     * Find all facts which match the given search query.
     * @param query Fact representing the search query
     * @return Matching facts
     */
    List<String> find(Fact query) throws UnregisteredConceptException;

    /**
     * Return a count of all facts which match the given search query.
     * @param query Fact representing the search query
     * @return Count of matching facts
     */
    long count(Fact query) throws UnregisteredConceptException;

    /**
     * Clears the DB. Useful for re-training and testing the web service.
     * @return True if successful
     */
    boolean deleteAll();
}
