package ch.unibe.scg.cells;

import java.io.Closeable;

import com.google.protobuf.ByteString;

/** A lookup table is a decoded {@link CellLookupTable}. Closing closes the underlying CellLookupTable. */
public interface LookupTable<T> extends Closeable {
	/** Return a table row. */
	Iterable<T> readRow(ByteString rowKey);

	/** @return a decoded table column. */
	Iterable<T> readColumn(ByteString columnKey);
}