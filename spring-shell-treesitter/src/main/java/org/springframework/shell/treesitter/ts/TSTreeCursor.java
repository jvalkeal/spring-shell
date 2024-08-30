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
 * struct TSTreeCursor {
 *     const void *tree;
 *     const void *id;
 *     uint32_t context[3];
 * }
 * }
 */
public class TSTreeCursor {

    TSTreeCursor() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        TreeSitter.C_POINTER.withName("tree"),
        TreeSitter.C_POINTER.withName("id"),
        MemoryLayout.sequenceLayout(3, TreeSitter.C_INT).withName("context"),
        MemoryLayout.paddingLayout(4)
    ).withName("TSTreeCursor");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final AddressLayout tree$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("tree"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * const void *tree
     * }
     */
    public static final AddressLayout tree$layout() {
        return tree$LAYOUT;
    }

    private static final long tree$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * const void *tree
     * }
     */
    public static final long tree$offset() {
        return tree$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * const void *tree
     * }
     */
    public static MemorySegment tree(MemorySegment struct) {
        return struct.get(tree$LAYOUT, tree$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * const void *tree
     * }
     */
    public static void tree(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(tree$LAYOUT, tree$OFFSET, fieldValue);
    }

    private static final AddressLayout id$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("id"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * const void *id
     * }
     */
    public static final AddressLayout id$layout() {
        return id$LAYOUT;
    }

    private static final long id$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * const void *id
     * }
     */
    public static final long id$offset() {
        return id$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * const void *id
     * }
     */
    public static MemorySegment id(MemorySegment struct) {
        return struct.get(id$LAYOUT, id$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * const void *id
     * }
     */
    public static void id(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(id$LAYOUT, id$OFFSET, fieldValue);
    }

    private static final SequenceLayout context$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("context"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint32_t context[3]
     * }
     */
    public static final SequenceLayout context$layout() {
        return context$LAYOUT;
    }

    private static final long context$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint32_t context[3]
     * }
     */
    public static final long context$offset() {
        return context$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint32_t context[3]
     * }
     */
    public static MemorySegment context(MemorySegment struct) {
        return struct.asSlice(context$OFFSET, context$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint32_t context[3]
     * }
     */
    public static void context(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, context$OFFSET, context$LAYOUT.byteSize());
    }

    private static long[] context$DIMS = { 3 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * uint32_t context[3]
     * }
     */
    public static long[] context$dimensions() {
        return context$DIMS;
    }
    private static final VarHandle context$ELEM_HANDLE = context$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * uint32_t context[3]
     * }
     */
    public static int context(MemorySegment struct, long index0) {
        return (int)context$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * uint32_t context[3]
     * }
     */
    public static void context(MemorySegment struct, long index0, int fieldValue) {
        context$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
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

