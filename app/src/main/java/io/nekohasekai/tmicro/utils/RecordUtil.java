package io.nekohasekai.tmicro.utils;

import io.nekohasekai.tmicro.utils.rms.RecordDatabase;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.IOException;

public class RecordUtil {

    public static RecordDatabase openPrivate(String name) throws IOException {
        try {
            return new RecordDatabase(RecordStore.openRecordStore(name, true, RecordStore.AUTHMODE_PRIVATE, false));
        } catch (RecordStoreException e) {
            throw new IOException(e.getMessage());
        }
    }

}
