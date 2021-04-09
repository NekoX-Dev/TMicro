package io.nekohasekai.tmicro.utils.rms;

import io.nekohasekai.tmicro.tmnet.SerializedData;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.IOException;

public class RecordDatabase {

    public RecordStore rs;

    public RecordDatabase(RecordStore rs) {
        this.rs = rs;
    }

    public byte[] getBytes(int index) throws IOException {
        try {
            if (index >= rs.getNumRecords()) throw new IOException("Not found");
            return rs.getRecord(index);
        } catch (RecordStoreException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public void setBytes(int index, byte[] bytes) throws IOException {
        try {
            if (index >= rs.getNumRecords()) {
                rs.addRecord(bytes, 0, bytes.length);
            } else {
                rs.setRecord(index, bytes, 0, bytes.length);
            }
        } catch (RecordStoreException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public SerializedData getIn(int index) throws IOException {
        return new SerializedData(getBytes(index));
    }

    public SerializedData getOut(final int index) {
        return new SerializedData() {
            public void flush() throws IOException {
                super.flush();
                setBytes(index, toByteArray());
            }
        };
    }

}
