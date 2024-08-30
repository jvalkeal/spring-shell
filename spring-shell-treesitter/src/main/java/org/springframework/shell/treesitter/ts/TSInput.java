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
 * struct TSInput {
 *     void *payload;
 *     const char *(*read)(void *, uint32_t, TSPoint, uint32_t *);
 *     TSInputEncoding encoding;
 * }
 * }
 */
public class TSInput {

    TSInput() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        TreeSitter.C_POINTER.withName("payload"),
        TreeSitter.C_POINTER.withName("read"),
        TreeSitter.C_INT.withName("encoding"),
        MemoryLayout.paddingLayout(4)
    ).withName("TSInput");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final AddressLayout payload$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("payload"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * void *payload
     * }
     */
    public static final AddressLayout payload$layout() {
        return payload$LAYOUT;
    }

    private static final long payload$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * void *payload
     * }
     */
    public static final long payload$offset() {
        return payload$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * void *payload
     * }
     */
    public static MemorySegment payload(MemorySegment struct) {
        return struct.get(payload$LAYOUT, payload$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * void *payload
     * }
     */
    public static void payload(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(payload$LAYOUT, payload$OFFSET, fieldValue);
    }

    /**
     * {@snippet lang=c :
     * const char *(*read)(void *, uint32_t, TSPoint, uint32_t *)
     * }
     */
    public static class read {

        read() {
            // Should not be called directly
        }

        /**
         * The function pointer signature, expressed as a functional interface
         */
        public interface Function {
            MemorySegment apply(MemorySegment _x0, int _x1, MemorySegment _x2, MemorySegment _x3);
        }

        private static final FunctionDescriptor $DESC = FunctionDescriptor.of(
            TreeSitter.C_POINTER,
            TreeSitter.C_POINTER,
            TreeSitter.C_INT,
            TSPoint.layout(),
            TreeSitter.C_POINTER
        );

        /**
         * The descriptor of this function pointer
         */
        public static FunctionDescriptor descriptor() {
            return $DESC;
        }

        private static final MethodHandle UP$MH = TreeSitter.upcallHandle(read.Function.class, "apply", $DESC);

        /**
         * Allocates a new upcall stub, whose implementation is defined by {@code fi}.
         * The lifetime of the returned segment is managed by {@code arena}
         */
        public static MemorySegment allocate(read.Function fi, Arena arena) {
            return Linker.nativeLinker().upcallStub(UP$MH.bindTo(fi), $DESC, arena);
        }

        private static final MethodHandle DOWN$MH = Linker.nativeLinker().downcallHandle($DESC);

        /**
         * Invoke the upcall stub {@code funcPtr}, with given parameters
         */
        public static MemorySegment invoke(MemorySegment funcPtr,MemorySegment _x0, int _x1, MemorySegment _x2, MemorySegment _x3) {
            try {
                return (MemorySegment) DOWN$MH.invokeExact(funcPtr, _x0, _x1, _x2, _x3);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        }
    }

    private static final AddressLayout read$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("read"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * const char *(*read)(void *, uint32_t, TSPoint, uint32_t *)
     * }
     */
    public static final AddressLayout read$layout() {
        return read$LAYOUT;
    }

    private static final long read$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * const char *(*read)(void *, uint32_t, TSPoint, uint32_t *)
     * }
     */
    public static final long read$offset() {
        return read$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * const char *(*read)(void *, uint32_t, TSPoint, uint32_t *)
     * }
     */
    public static MemorySegment read(MemorySegment struct) {
        return struct.get(read$LAYOUT, read$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * const char *(*read)(void *, uint32_t, TSPoint, uint32_t *)
     * }
     */
    public static void read(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(read$LAYOUT, read$OFFSET, fieldValue);
    }

    private static final OfInt encoding$LAYOUT = (OfInt)$LAYOUT.select(groupElement("encoding"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * TSInputEncoding encoding
     * }
     */
    public static final OfInt encoding$layout() {
        return encoding$LAYOUT;
    }

    private static final long encoding$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * TSInputEncoding encoding
     * }
     */
    public static final long encoding$offset() {
        return encoding$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * TSInputEncoding encoding
     * }
     */
    public static int encoding(MemorySegment struct) {
        return struct.get(encoding$LAYOUT, encoding$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * TSInputEncoding encoding
     * }
     */
    public static void encoding(MemorySegment struct, int fieldValue) {
        struct.set(encoding$LAYOUT, encoding$OFFSET, fieldValue);
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
