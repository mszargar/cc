package ch.unibe.scg.cc.activerecord;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

public class HTableProvider implements Provider<HTable> {

	@Inject @Named("tableName")
	String tableName;
	
	@Override
	public HTable get() {
		Configuration hbaseConfig = HBaseConfiguration.create();
        HTable htable;
		try {
			htable = new HTable(hbaseConfig, tableName);
			htable.setAutoFlush(false);
			htable.setWriteBufferSize(1024 * 1024 * 12);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return htable;
	}

}
