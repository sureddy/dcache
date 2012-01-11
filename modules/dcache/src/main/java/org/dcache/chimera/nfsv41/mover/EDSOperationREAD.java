package org.dcache.chimera.nfsv41.mover;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dcache.chimera.nfs.v4.AbstractNFSv4Operation;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.nfsstat;
import org.dcache.chimera.nfs.v4.CompoundContext;
import org.dcache.chimera.nfs.v4.xdr.*;
import org.dcache.pool.repository.RepositoryChannel;

public class EDSOperationREAD extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(EDSOperationREAD.class.getName());

     private final Map<stateid4, MoverBridge> _activeIO;

    public EDSOperationREAD(nfs_argop4 args,  Map<stateid4, MoverBridge> activeIO) {
        super(args, nfs_opnum4.OP_READ);
        _activeIO = activeIO;
    }

    @Override
    public nfs_resop4 process(CompoundContext context) {
        READ4res res = new READ4res();

        try {

            long offset = _args.opread.offset.value.value;
            int count = _args.opread.count.value.value;

            MoverBridge moverBridge = _activeIO.get(_args.opread.stateid);
            if(moverBridge == null) {
                throw new ChimeraNFSException(nfsstat.NFSERR_BAD_STATEID,
                        "No mover associated with given stateid");
            }

            ByteBuffer bb = ByteBuffer.allocate(count);
            RepositoryChannel fc = moverBridge.getFileChannel();

            bb.rewind();
            int bytesReaded = fc.read(bb, offset);

            moverBridge.getMover().setBytesTransferred(bytesReaded);

            res.status = nfsstat.NFS_OK;
            res.resok4 = new READ4resok();
            res.resok4.data = bb;

            if( offset + bytesReaded == fc.size() ) {
                res.resok4.eof = true;
            }

            _log.debug("MOVER: {}@{} readed, {} requested.",
                    new Object[]{
                        bytesReaded,
                        offset,
                        _args.opread.count.value.value
                    });

        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
            _log.debug(he.getMessage());
        }catch(IOException ioe) {
            _log.error("DSREAD: ", ioe);
            res.status = nfsstat.NFSERR_IO;
        }catch(Exception e) {
            _log.error("DSREAD: ", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
        }

       _result.opread = res;
        return _result;
    }
}