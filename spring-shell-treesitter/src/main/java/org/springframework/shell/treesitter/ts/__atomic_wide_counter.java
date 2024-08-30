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
 * union {
 *     unsigned long long __value64;
 *     struct {
 *         unsigned int __low;
 *         unsigned int __high;
 *     } __value32;
 * }
 * }
 */
public class __atomic_wide_counter {

    __atomic_wide_counter() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.unionLayout(
        TreeSitter.C_LONG_LONG.withName("__value64"),
        __atomic_wide_counter.__value32.layout().withName("__value32")
    ).withName("$anon$25:9");

    /**
     * The layout of this union
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfLong __value64$LAYOUT = (OfLong)$LAYOUT.select(groupElement("__value64"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned long long __value64
     * }
     */
    public static final OfLong __value64$layout() {
        return __value64$LAYOUT;
    }

    private static final long __value64$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned long long __value64
     * }
     */
    public static final long __value64$offset() {
        return __value64$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned long long __value64
     * }
     */
    public static long __value64(MemorySegment union) {
        return union.get(__value64$LAYOUT, __value64$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned long long __value64
     * }
     */
    public static void __value64(MemorySegment union, long fieldValue) {
        union.set(__value64$LAYOUT, __value64$OFFSET, fieldValue);
    }

    /**
     * {@snippet lang=c :
     * struct {
     *     unsigned int __low;
     *     unsigned int __high;
     * }
     * }
     */
    public static class __value32 {

        __value32() {
            // Should not be called directly
        }

        private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
            TreeSitter.C_INT.withName("__low"),
            TreeSitter.C_INT.withName("__high")
        ).withName("$anon$28:3");

        /**
         * The layout of this struct
         */
        public static final GroupLayout layout() {
            return $LAYOUT;
        }

        private static final OfInt __low$LAYOUT = (OfInt)$LAYOUT.select(groupElement("__low"));

        /**
         * Layout for field:
         * {@snippet lang=c :
         * unsigned int __low
         * }
         */
        public static final OfInt __low$layout() {
            return __low$LAYOUT;
        }

        private static final long __low$OFFSET = 0;

        /**
         * Offset for field:
         * {@snippet lang=c :
         * unsigned int __low
         * }
         */
        public static final long __low$offset() {
            return __low$OFFSET;
        }

        /**
         * Getter for field:
         * {@snippet lang=c :
         * unsigned int __low
         * }
         */
        public static int __low(MemorySegment struct) {
            return struct.get(__low$LAYOUT, __low$OFFSET);
        }

        /**
         * Setter for field:
         * {@snippet lang=c :
         * unsigned int __low
         * }
         */
        public static void __low(MemorySegment struct, int fieldValue) {
            struct.set(__low$LAYOUT, __low$OFFSET, fieldValue);
        }

        private static final OfInt __high$LAYOUT = (OfInt)$LAYOUT.select(groupElement("__high"));

        /**
         * Layout for field:
         * {@snippet lang=c :
         * unsigned int __high
         * }
         */
        public static final OfInt __high$layout() {
            return __high$LAYOUT;
        }

        private static final long __high$OFFSET = 4;

        /**
         * Offset for field:
         * {@snippet lang=c :
         * unsigned int __high
         * }
         */
        public static final long __high$offset() {
            return __high$OFFSET;
        }

        /**
         * Getter for field:
         * {@snippet lang=c :
         * unsigned int __high
         * }
         */
        public static int __high(MemorySegment struct) {
            return struct.get(__high$LAYOUT, __high$OFFSET);
        }

        /**
         * Setter for field:
         * {@snippet lang=c :
         * unsigned int __high
         * }
         */
        public static void __high(MemorySegment struct, int fieldValue) {
            struct.set(__high$LAYOUT, __high$OFFSET, fieldValue);
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

    private static final GroupLayout __value32$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("__value32"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int __low;
     *     unsigned int __high;
     * } __value32
     * }
     */
    public static final GroupLayout __value32$layout() {
        return __value32$LAYOUT;
    }

    private static final long __value32$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int __low;
     *     unsigned int __high;
     * } __value32
     * }
     */
    public static final long __value32$offset() {
        return __value32$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int __low;
     *     unsigned int __high;
     * } __value32
     * }
     */
    public static MemorySegment __value32(MemorySegment union) {
        return union.asSlice(__value32$OFFSET, __value32$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int __low;
     *     unsigned int __high;
     * } __value32
     * }
     */
    public static void __value32(MemorySegment union, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, union, __value32$OFFSET, __value32$LAYOUT.byteSize());
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this union
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

