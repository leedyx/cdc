package org.lee.cdc.context;

import lombok.Data;

@Data
public class SyncContext {

    private int errorCount = 0;

    private int count = 0;

    public int incrementCount() {
        return ++count;
    }


}
