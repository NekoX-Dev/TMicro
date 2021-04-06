package io.nekohasekai.tmicro.utils.rms;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.*;

public class RecordDatabase {

    public RecordStore rs;

    public RecordDatabase(RecordStore rs) {
        this.rs = rs;
    }

    public byte[] getBytes(int index) throws IOException {
        try {
            return rs.getRecord(index);
        } catch (RecordStoreException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void setBytes(int index, byte[] bytes) throws IOException {
        try {
            rs.setRecord(index, bytes, 0, bytes.length);
        } catch (RecordStoreException e) {
            throw new IOException(e.getMessage());
        }
    }

    public DataInputStream getIn(int index) throws IOException {
        return new DataInputStream(new ByteArrayInputStream(getBytes(index)));
    }

    public DataOutputStream getOut(final int index) {
        return new DataOutputStream(new ByteArrayOutputStream() {
            public void flush() throws IOException {
                setBytes(index, toByteArray());
            }
        });
    }

}
