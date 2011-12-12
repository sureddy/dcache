/*
 * Automatically generated by jrpcgen 1.0.7 on 2/21/09 1:22 AM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
package org.dcache.chimera.nfs.v4.xdr;
import org.dcache.xdr.*;
import java.io.IOException;

public class exist_lock_owner4 implements XdrAble {
    public stateid4 lock_stateid;
    public seqid4 lock_seqid;

    public exist_lock_owner4() {
    }

    public exist_lock_owner4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        lock_stateid.xdrEncode(xdr);
        lock_seqid.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        lock_stateid = new stateid4(xdr);
        lock_seqid = new seqid4(xdr);
    }

}
// End of exist_lock_owner4.java