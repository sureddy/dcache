package org.dcache.pool.classic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import dmg.util.Args;

import diskCacheV111.util.CacheException;
import diskCacheV111.util.DiskErrorCacheException;
import diskCacheV111.util.PnfsId;
import diskCacheV111.util.PnfsHandler;
import diskCacheV111.util.FileNotInCacheException;
import diskCacheV111.util.NotInTrashCacheException;
import diskCacheV111.util.TimeoutCacheException;
import diskCacheV111.vehicles.StorageInfo;

import org.dcache.cells.CellCommandListener;
import org.dcache.cells.CellLifeCycleAware;
import org.dcache.util.Checksum;
import org.dcache.util.ChecksumType;
import org.dcache.namespace.FileAttribute;

import org.dcache.pool.repository.CacheEntry;
import org.dcache.pool.repository.EntryState;
import org.dcache.pool.repository.IllegalTransitionException;
import org.dcache.pool.repository.Repository;
import org.dcache.pool.repository.Repository.OpenFlags;
import org.dcache.pool.repository.ReplicaDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecksumScanner
    implements CellCommandListener, CellLifeCycleAware
{
    private final static Logger _log =
        LoggerFactory.getLogger(ChecksumScanner.class);

    private final FullScan _fullScan = new FullScan();
    private final Scrubber _scrubber = new Scrubber();
    private final SingleScan _singleScan = new SingleScan();

    private Repository _repository;
    private PnfsHandler _pnfs;
    private ChecksumModuleV1 _csm;

    private File _scrubberStateFile;

    /** Errors found while running 'csm check'.
     */
    private final Map<PnfsId,Checksum> _bad =
        new ConcurrentHashMap<PnfsId,Checksum>();

    public void startScrubber()
    {
        _scrubber.start();
    }

    public void stopScrubber()
    {
        _scrubber.kill();
    }

    public void setRepository(Repository repository)
    {
        _repository = repository;
    }

    public void setPnfs(PnfsHandler pnfs)
    {
        _pnfs = pnfs;
    }

    public void setChecksumModule(ChecksumModuleV1 csm)
    {
        _csm = csm;
    }

    public void setScrubberStateFile(File path)
    {
        _scrubberStateFile = path;
    }

    private Checksum checkFile(File file, double throughputLimit)
        throws IOException, InterruptedException
    {
        return _csm.getDefaultChecksumFactory()
            .computeChecksum(file, throughputLimit);
    }

    private Checksum readChecksum(CacheEntry entry) throws CacheException
    {
        StorageInfo info = entry.getStorageInfo();
        String flags = (info == null ? null : info.getKey("flag-c"));

        if (flags == null) {
            return _csm.getDefaultChecksumFactory().find(_pnfs.getFileAttributes(entry.getPnfsId(), EnumSet.of(FileAttribute.CHECKSUM)).getChecksums());
        } else {
            return Checksum.parseChecksum(flags);
        }
    }

    private class FullScan extends Singleton
    {
        private volatile int _totalCount;
        private volatile int _badCount;

        public FullScan()
        {
            super("FullScan");
        }

        public void runIt() throws Exception
        {
            stopScrubber();
            try {
                _totalCount = _badCount = 0;
                _bad.clear();

                for (PnfsId id: _repository) {
                    try {
                        ReplicaDescriptor handle =
                            _repository.openEntry(id, EnumSet.of(OpenFlags.NOATIME));
                        try {
                            Checksum replica =
                                checkFile(handle.getFile(),
                                          Double.POSITIVE_INFINITY);
                            Checksum file = readChecksum(handle.getEntry());
                            _totalCount++;

                            if (file != null && !file.equals(replica)) {
                                _bad.put(id, replica);
                                _badCount++;
                            }
                        } finally {
                            handle.close();
                        }
                    } catch (FileNotInCacheException e) {
                        /* It was removed before we could get it. No problem.
                         */
                    } catch (NotInTrashCacheException e) {
                        /* orphan or lost file. Not our problem.
                         */
                    }
                }
            } finally {
                startScrubber();
            }
        }

        public String toString()
        {
            return super.toString() + " "
                + _totalCount + " checked; "
                + _badCount + " errors detected";
        }
    }

    private class SingleScan extends Singleton
    {
        private volatile PnfsId _pnfsId;
        private volatile Checksum _fileCRC;
        private volatile Checksum _infoCRC;

        public SingleScan()
        {
            super("SingeScan");
        }

        public synchronized void go(PnfsId pnfsId) throws Exception
        {
            _pnfsId = pnfsId;
            start();
        }

        public void runIt() throws Exception
        {
            stopScrubber();
            try {
                _fileCRC = null;
                _infoCRC = null;
                ReplicaDescriptor handle =
                    _repository.openEntry(_pnfsId, EnumSet.of(OpenFlags.NOATIME));
                try {
                    _fileCRC = checkFile(handle.getFile(), Double.POSITIVE_INFINITY);
                    _infoCRC = readChecksum(handle.getEntry());
                    if (_infoCRC != null && !_infoCRC.equals(_fileCRC)) {
                        _bad.put(_pnfsId, _fileCRC);
                    }
                } finally {
                    handle.close();
                }
            } finally {
                startScrubber();
            }
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (_pnfsId != null) {
                sb.append("  ").append(_pnfsId).append(" ");
                if ((_fileCRC != null) && (_infoCRC == null)) {
                    sb.append("No StorageInfo, crc = ").append(_fileCRC);
                } else if ((_fileCRC == null) || (_infoCRC == null)) {
                    sb.append("BUSY");
                } else if (_fileCRC.equals(_infoCRC)) {
                    sb.append("OK ").append(_fileCRC.toString());
                } else {
                    sb.append("BAD File = ").append(_fileCRC)
                        .append(" Expected = ").append(_infoCRC);
                }
            }
            return sb.toString();
        }
    }

    private class Scrubber extends Singleton
    {
        private final long CHECKPOINT_INTERVAL = TimeUnit.MINUTES.toMillis(1);
        private final long FAILURE_RATELIMIT_DELAY =
            TimeUnit.SECONDS.toMillis(10);

        private volatile int _badCount;
        private volatile int _numFiles;
        private volatile int _totalCount;

        private PnfsId _lastFileChecked;
        private long _lastCheckpoint;
        private long _lastStart;

        public Scrubber()
        {
            super("Scrubber");
        }

        /**
         * Save scrubber state to <code>_scrubberStateFile</code>. The format is
         * the start time of the last scrub (<code>_lastStart</code>) separated
         * by a whitespace followed by the pnfs id of the file last checked
         * (<code>_lastFileChecked</code>). If there's no last checked file,
         * write a dash instead.
         */
        private void saveState()
        {
            String line = _lastStart + " " +
                          ((_lastFileChecked == null) ? "-" : _lastFileChecked);
            try {
                Files.write(line, _scrubberStateFile, Charset.defaultCharset());
            } catch (IOException e) {
                _log.error("Failed to save scrubber state ({}) to {}: {}",
                           new Object[] { line,
                                          _scrubberStateFile,
                                          e.getMessage() });
            }
        }

        /**
         * Read the saved state information from disk written by <code>
         * saveState()</code>. The following fields are initialized:<code>
         * _lastFileChecked</code> - the pnfs id of the file that was last
         * checksummed; <code>_lastStart</code> - time when the last scrub
         * started, if there's no saved state it's initialized to the current
         * time.
         */
        private void initializeFromSavedState()
        {
            String line;
            try {
                line = Files.readFirstLine(_scrubberStateFile,
                                           Charset.defaultCharset());
            } catch (FileNotFoundException e) {
                /**
                 * ignored - start immediately and check whole pool
                 */
                _lastStart = System.currentTimeMillis();
                return;
            } catch (IOException e) {
                _log.error("Failed to read scrubber saved state from {}: {}",
                          _scrubberStateFile, e.getMessage());
                return;
            }

            String[] fields = line.split(" ");
            if (fields.length != 2) {
                _log.error("scrubber saved state in {} has an invalid format: {}",
                          _scrubberStateFile, line);
                return;
            }

            try {
                _lastStart = Long.parseLong(fields[0]);
            } catch (NumberFormatException e) {
                _log.error("Failed to read the last scrubber start time from {}: {}",
                          _scrubberStateFile, e.getMessage());
                return;
            }

            if (PnfsId.isValid(fields[1])) {
                _log.debug("Resuming scrubbing from the first file with a pnfs id greater than {}",
                           fields[1]);
                _lastFileChecked = new PnfsId(fields[1]);
            } else if (!fields[1].equals("-")) {
                _log.error("Last checked pnfs id within {} has an invalid format: {}",
                           _scrubberStateFile, fields[1]);
            }
        }

        private boolean isFirstStart()
        {
            return !_scrubberStateFile.exists();
        }

        private boolean isResuming()
        {
            return (_lastFileChecked != null);
        }

        private void waitUntil(long t) throws InterruptedException
        {
            long now;
            while ((now = System.currentTimeMillis()) < t) {
                Thread.sleep(t - now);
            }
        }

        @Override
        public synchronized void start()
        {
            if (_csm.isScrubbingEnabled() && !isActive()) {
                super.start();
            }
        }

        @Override
        public void runIt() throws InterruptedException
        {
            initializeFromSavedState();
            boolean isFinished = !isFirstStart() && !isResuming();

            try {
                while (true) {
                    if (isFinished) {
                        _log.debug("Next scrub start is {}",
                             new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").
                                 format(new Date(_lastStart +
                                                 _csm.getScrubPeriod())));
                        if (System.currentTimeMillis() - _lastStart > _csm.getScrubPeriod()) {
                            _log.warn("The last scrub took longer time to finish ({} s.) than the configured period ({} s.) - consider increasing the scrubbing period",
                                      System.currentTimeMillis() - _lastStart,
                                      _csm.getScrubPeriod());
                        }
                        waitUntil(_lastStart + _csm.getScrubPeriod());
                        _lastStart = System.currentTimeMillis();
                        isFinished = false;
                    }

                    try {
                        PnfsId[] toScan = getFilesToVerify();
                        _numFiles = toScan.length;
                        _badCount = 0;
                        _totalCount = 0;
                        scanFiles(toScan);
                        if (_badCount > 0) {
                            _log.warn("Finished scrubbing. Found {} bad files of {}",
                                       _badCount, _numFiles);
                        }
                        isFinished = true;
                    } catch (IllegalStateException e) {
                        Thread.sleep(FAILURE_RATELIMIT_DELAY);
                    } catch (TimeoutCacheException e) {
                        Thread.sleep(FAILURE_RATELIMIT_DELAY);
                    } catch (CacheException e) {
                        _log.error("Received an unexpected error during scrubbing: {}",
                                   e.getMessage());
                        Thread.sleep(FAILURE_RATELIMIT_DELAY);
                    }
                }
            } finally {
                _log.debug("Stopping scrubber");
                saveState();
            }
        }

        /**
         * Return array of pnfs id's that has not yet been verified. Any files
         * added to the pool after this array has been generated will be
         * included the next time the array is generated.
         * @return array of pnfs id's that needs to be verified. No check is
         *         done on in which state the files are in.
         */
        private PnfsId[] getFilesToVerify()
        {
            PnfsId[] repcopy = Iterables.toArray(_repository, PnfsId.class);
            Arrays.sort(repcopy);

            if (!isResuming()) {
                return repcopy;
            }

            int index = Arrays.binarySearch(repcopy, _lastFileChecked);
            if (index >= 0) {
                /**
                 * Found. 0 <= index <= repcopy.length - 1
                 */
                return Arrays.copyOfRange(repcopy, index + 1, repcopy.length);
            } else {
                /**
                 * Not found. insertionPoint == -index - 1
                 * where: 0 <= insertionPoint <= repcopy.length
                 */
                return Arrays.copyOfRange(repcopy, -index - 1, repcopy.length);
            }
        }

        private boolean isChecksumOk(PnfsId id)
            throws InterruptedException, CacheException
        {
            ReplicaDescriptor handle =
                _repository.openEntry(id, EnumSet.of(OpenFlags.NOATIME));
            try {
                Checksum storedChecksum = readChecksum(handle.getEntry());
                if (storedChecksum == null) {
                    return true;
                }
                Checksum newChecksum;
                File file = handle.getFile();
                newChecksum = checkFile(file, _csm.getThroughputLimit());
                return storedChecksum.equals(newChecksum);
            } catch (IOException e) {
                throw new DiskErrorCacheException("Failed to checksum " +
                                                  id + ": " +
                                                  e.getMessage());
            } finally {
                handle.close();
            }
        }

        /**
         * Save state information only every <code>CHECKPOINT_INTERVAL</code>
         * period.
         */
        private void checkpointIfNeeded()
        {
            if (System.currentTimeMillis() - _lastCheckpoint > CHECKPOINT_INTERVAL) {
                saveState();
                _lastCheckpoint = System.currentTimeMillis();
            }
        }

        private void scanFiles(PnfsId[] repository)
            throws InterruptedException, CacheException
        {
            for (PnfsId id : repository) {
                if (_repository.getState(id) == EntryState.CACHED ||
                    _repository.getState(id) == EntryState.PRECIOUS) {
                    try {
                        if (!isChecksumOk(id)) {
                            _badCount++;
                            /**
                             * TODO: Need to use special log target hierarchy
                             * for admin notification.
                             */
                            _log.error("Checksum mismatch detected for {} - marking as BROKEN",
                                       id);
                            try {
                                _repository.setState(id, EntryState.BROKEN);
                            } catch (IllegalTransitionException e) {
                                _log.warn("Failed to mark {} as BROKEN: {}",
                                          id, e.getMessage());
                            }
                        }
                        _lastFileChecked = id;
                    } catch (FileNotInCacheException e) {
                        /**
                         * It was removed before we could get it. No problem.
                         */
                    } catch (DiskErrorCacheException e) {
                        _log.error("Failed to verify the checksum of {} (skipping): {}",
                                   id, e.getMessage());
                    }
                }
                _totalCount++;
                checkpointIfNeeded();
            }
            _lastFileChecked = null;
        }

        @Override
        public String toString()
        {
            return super.toString() + " " + _totalCount + " of " + _numFiles +
                " checked; " + _badCount + " errors detected";
        }
    }

    abstract private class Singleton
    {
        private final String  _name;

        private Exception _lastException;
        private Thread _currentThread;

        private Singleton(String name)
        {
            _name = name;
        }

        abstract protected void runIt() throws Exception;

        public synchronized void kill()
        {
            if (isActive()) {
                _currentThread.interrupt();
            }
        }

        public synchronized boolean isActive()
        {
            return (_currentThread != null);
        }

        private synchronized void stopped()
        {
            _currentThread = null;
        }

        public synchronized void setException(Exception exception)
        {
            _lastException = exception;
            _log.error(exception.toString());
        }

        public synchronized Exception getException()
        {
            return _lastException;
        }

        public synchronized void start()
        {
            if (isActive()) {
                throw new IllegalStateException("Still active");
            }
            _lastException = null;
            _currentThread = new Thread(_name) {
                    public void run() {
                        try {
                            runIt();
                        } catch (Exception ee) {
                            _lastException = ee;
                        } finally {
                            stopped();
                        }
                    }
                };
            _currentThread.start();
        }

        public synchronized String toString()
        {
            return _name + (isActive() ? " Active " : " Idle ") +
                (_lastException == null ? "" : _lastException.toString());
        }
    }

    public final static String hh_csm_check = " [ * | <pnfsId> ]";
    public String ac_csm_check_$_1(Args args) throws Exception
    {
        if (args.argv(0).equals("*")) {
            _fullScan.start();
        } else {
            _singleScan.go(new PnfsId(args.argv(0)));
        }
        return "Started ...; check 'csm status' for status";
    }

    public String ac_csm_status(Args args)
    {
        return _fullScan.toString() + "\n" + _singleScan.toString() + "\n" +
            _scrubber.toString();
    }

    public final static String hh_csm_show_errors =
        "# show errors found with 'csm check'";
    public String ac_csm_show_errors(Args args)
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<PnfsId,Checksum> e: _bad.entrySet()) {
            builder
                .append(e.getKey())
                .append(" -> ")
                .append(e.getValue())
                .append('\n');
        }
        return builder.toString();
    }

    @Override
    public void afterStart()
    {
        startScrubber();
    }

    @Override
    public void beforeStop()
    {
        stopScrubber();
    }
}