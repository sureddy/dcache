/*
 * Automatically generated by jrpcgen 1.0.7 on 2/21/09 1:22 AM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
package org.dcache.chimera.nfs.v4.xdr;
import org.dcache.chimera.nfs.v4.*;
import org.dcache.xdr.*;
import java.io.IOException;

public class LOCK4denied implements XdrAble {
    public offset4 offset;
    public length4 length;
    public int locktype;
    public lock_owner4 owner;

    public LOCK4denied() {
    }

    public LOCK4denied(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        offset.xdrEncode(xdr);
        length.xdrEncode(xdr);
        xdr.xdrEncodeInt(locktype);
        owner.xdrEncode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        offset = new offset4(xdr);
        length = new length4(xdr);
        locktype = xdr.xdrDecodeInt();
        owner = new lock_owner4(xdr);
    }

}
// End of LOCK4denied.java