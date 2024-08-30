// Generated by jextract

package org.springframework.shell.treesitter.ts;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * struct {
 *     TSWasmErrorKind kind;
 *     char *message;
 * }
 * }
 */
public class TSWasmError {

    TSWasmError() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        TreeSitter.C_INT.withName("kind"),
        MemoryLayout.paddingLayout(4),
        TreeSitter.C_POINTER.withName("message")
    ).withName("$anon$1189:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt kind$LAYOUT = (OfInt)$LAYOUT.select(groupElement("kind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * TSWasmErrorKind kind
     * }
     */
    public static final OfInt kind$layout() {
        return kind$LAYOUT;
    }

    private static final long kind$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * TSWasmErrorKind kind
     * }
     */
    public static final long kind$offset() {
        return kind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * TSWasmErrorKind kind
     * }
     */
    public static int kind(MemorySegment struct) {
        return struct.get(kind$LAYOUT, kind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * TSWasmErrorKind kind
     * }
     */
    public static void kind(MemorySegment struct, int fieldValue) {
        struct.set(kind$LAYOUT, kind$OFFSET, fieldValue);
    }

    private static final AddressLayout message$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("message"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * char *message
     * }
     */
    public static final AddressLayout message$layout() {
        return message$LAYOUT;
    }

    private static final long message$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * char *message
     * }
     */
    public static final long message$offset() {
        return message$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * char *message
     * }
     */
    public static MemorySegment message(MemorySegment struct) {
        return struct.get(message$LAYOUT, message$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * char *message
     * }
     */
    public static void message(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(message$LAYOUT, message$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}

