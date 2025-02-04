package me.darragh.lwjgl.event;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.LWJGLUtil;

/**
 * A circular buffer working with integers for the purpose of storing and managing the current count of events across multiple arrays.
 * <p>
 * This is a <i>weird</i> solution, but is effective.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@Data
@RequiredArgsConstructor
public class CircularEventQueue {
    @Getter
    private final int maxCapacity;

    @Getter
    private int currentCount = 0;

    @Getter
    private int readPosition = 0,
            writePosition = 1;

    private long lastDroppedMessage = 0L;

    /**
     * Increments the current count and updates the array write position.
     *
     * @since 1.0.0
     *
     * @apiNote This method is synchronized to prevent errors when multiple threads are accessing the buffer.
     */
    public synchronized void push() {
        this.currentCount++;
        if (this.currentCount > this.maxCapacity) {
            this.currentCount = this.maxCapacity;

            // Display log message
            long current = System.currentTimeMillis();
            if (current - this.lastDroppedMessage > 1000L) {
                this.lastDroppedMessage = current;
                LWJGLUtil.log("Dropping extend (events) due to insufficient polling frequency.");
            }
        }

        this.writePosition = (this.writePosition + 1) % this.maxCapacity;
        if (this.writePosition == this.readPosition) {
            this.readPosition = (this.readPosition + 1) % this.maxCapacity;
            this.currentCount--;
        }
    }

    /**
     * Decrements the current count and updates the array read position.
     *
     * @since 1.0.0
     *
     * @return Whether there is an event to read.
     *
     * @apiNote This method is synchronized to prevent errors when multiple threads are accessing the buffer.
     */
    public synchronized boolean next() {
        if (this.currentCount == 0) {
            return false;
        }

        this.currentCount--;
        this.readPosition = (this.readPosition + 1) % this.maxCapacity;
        return true;
    }

    /**
     * Retrieves the last written position.
     *
     * @return The last written position.
     */
    public int getLastWritePosition() {
        return (this.writePosition + this.maxCapacity - 1) % this.maxCapacity;
    }

    /**
     * Retrieves the last read position.
     *
     * @return The last read position.
     */
    public int getLastReadPosition() {
        return (this.readPosition + this.maxCapacity - 1) % this.maxCapacity;
    }
}
