package org.dcache.pool.repository;

import java.util.Collection;

import diskCacheV111.util.CacheException;
import diskCacheV111.util.PnfsId;

/**
 * The MetaDataStore interface provides an abstraction of how
 * MetaDataRecord objects are created, retrieved and removed.
 *
 * The name is misleading and should be renamed. The reason is that
 * the interface is used as an abstraction over both meta data storage
 * and file storage.
 */
public interface MetaDataStore
{
    /**
     * Returns a collection of PNFS ids of available entries.
     */
    Collection<PnfsId> list();

    /**
     * Retrieves an existing entry previously created with
     * <i>create</i>.
     *
     * @param id PNFS id for which to retrieve the entry.
     * @return The entry or null if the entry does not exist.
     * @throws CacheException if looking up the entry failed.
     * @throws InterruptedException if the thread is interrupted.
     */
    MetaDataRecord get(PnfsId id)
        throws CacheException, InterruptedException;

    /**
     * Creates a new entry. The entry must not exist prior to this
     * call.
     *
     * @param id PNFS id for which to create the entry
     * @return The new entry
     * @throws DuplicateEntryException if entry already exists
     * @throws CacheException if entry creation fails
     */
    MetaDataRecord create(PnfsId id)
        throws DuplicateEntryException, CacheException;

    /**
     * Creates a new entry from an existing entry. The new entry will
     * have the same PNFS id and the same field values as the existing
     * entry.
     *
     * No entry with the same PNFS id must exist in the store prior to
     * this call. This implies that the existing entry must be from
     * another store.
     *
     * Typically used by the entry healer to import old entries into a
     * new store.
     *
     * Limitations:
     * <ul>
     * <li> Due to limitations in the CacheRepositoryEntry interface, the
     * locking status is currently not copied to the new entry.
     * <li> The storage info in both entries will refer to the same
     * storage info object.
     * <li> The sending to client flag is not preserved.
     * </ul>
     *
     * @param entry Cache entry from which to create the new entry
     * @return The new entry
     * @throws DuplicateEntryException if entry already exists
     * @throws CacheException if reading from <i>entry</i> fails
     */
    MetaDataRecord create(MetaDataRecord entry)
        throws DuplicateEntryException, CacheException;

    /**
     * Removes a meta data entry. If the entry does not exist, nothing
     * happens.
     *
     * @param id PNFS id of the entry to return.
     */
    void remove(PnfsId id);

    /**
     * Returns whether the store appears healthy. How this is
     * determined is up to the implementation.
     */
    boolean isOk();

    /** Closes the store and frees any associated resources. */
    void close();

    /**
     * Provides the amount of free space on the file system containing
     * the data files.
     */
    long getFreeSpace();

    /**
     * Provides the total amount of space on the file system
     * containing the data files.
     */
    long getTotalSpace();
}
