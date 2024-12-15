import java.util.*;

public class HeapManager {
    private int[] heap;
    private List<FreeBlock> freeList;

    public HeapManager(int[] heap) {
        this.heap = heap;
        this.freeList = new ArrayList<>();
        freeList.add(new FreeBlock(0, heap.length));
    }

    public int allocate(int size, boolean useBestFit) {
        FreeBlock bestBlock = null;
        FreeBlock firstBlock = null;

        System.out.println("Attempting to allocate size: " + size + (useBestFit ? " (Best-fit)" : " (First-fit)"));
        printFreeList();

        for (FreeBlock block : freeList) {
            if (block.size >= size) {
                if (!useBestFit) {
                    firstBlock = block;
                    break;
                }
                if (bestBlock == null || block.size < bestBlock.size) {
                    bestBlock = block;
                }
            }
        }

        FreeBlock selectedBlock = useBestFit ? bestBlock : firstBlock;
        if (selectedBlock == null) {
            System.out.println("Allocation failed: No suitable block found.");
            throw new RuntimeException("No suitable block found for allocation.");
        }

        int startAddress = selectedBlock.start;
        if (selectedBlock.size > size) {
            selectedBlock.start += size;
            selectedBlock.size -= size;
        } else {
            freeList.remove(selectedBlock);
        }

        System.out.println("Allocation succeeded at address: " + startAddress);
        printFreeList();
        return startAddress;
    }

    public void deallocate(int start, int size) {
        System.out.println("Deallocating block at address: " + start + " with size: " + size);
        FreeBlock newBlock = new FreeBlock(start, size);
        freeList.add(newBlock);
        coalesceFreeList();
        printFreeList();
    }

    private void coalesceFreeList() {
        freeList.sort(Comparator.comparingInt(block -> block.start));
        List<FreeBlock> mergedList = new ArrayList<>();
        FreeBlock prev = null;

        for (FreeBlock block : freeList) {
            if (prev == null || prev.start + prev.size < block.start) {
                mergedList.add(block);
                prev = block;
            } else {
                prev.size += block.size;
            }
        }
        freeList = mergedList;
    }

    private void printFreeList() {
        System.out.println("Current free list:");
        for (FreeBlock block : freeList) {
            System.out.println("  Start: " + block.start + ", Size: " + block.size);
        }
    }

    private static class FreeBlock {
        int start, size;

        FreeBlock(int start, int size) {
            this.start = start;
            this.size = size;
        }
    }

    public static void main(String[] args) {
        // Test for first-fit and best-fit differences
        int[] heap = new int[11];
        HeapManager managerFirstFit = new HeapManager(heap);
        HeapManager managerBestFit = new HeapManager(heap);

        // Allocation with first-fit and best-fit strategy
        System.out.println("First-fit:");
        try {
            int a = managerFirstFit.allocate(4, false);
            int b = managerFirstFit.allocate(1, false);
            int c = managerFirstFit.allocate(3, false);
            managerFirstFit.deallocate(a, 4);
            managerFirstFit.deallocate(c, 3);
            int d = managerFirstFit.allocate(5, false); // Should succeed
            System.out.println("Allocated at: " + d);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nBest-fit:");
        try {
            int a = managerBestFit.allocate(4, true);
            int b = managerBestFit.allocate(1, true);
            int c = managerBestFit.allocate(3, true);
            managerBestFit.deallocate(a, 4);
            managerBestFit.deallocate(c, 3);
            int d = managerBestFit.allocate(5, true); // Should fail
            System.out.println("Allocated at: " + d);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
