package org.dcache.xrootd.protocol.messages;
import org.dcache.xrootd.protocol.XrootdProtocol;

public class CloseRequest extends AbstractRequestMessage {

    public CloseRequest(int[] h, byte[] d) {
        super(h, d);

        if (getRequestID() != XrootdProtocol.kXR_close)
            throw new IllegalArgumentException(
                                               "doesn't seem to be a kXR_close message");
    }

    // public String getFileHandle() {
    //
    // readFromHeader(true);
    //
    // StringBuffer sb = new StringBuffer();
    //
    // for (int i = 4; i < 8; i++)
    // sb.append((char) getUnsignedChar(i));
    //
    // return sb.toString();
    // }

    public int getFileHandle() {

        readFromHeader(true);

        return getSignedInt(4);

    }

}