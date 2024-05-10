package com.issyzone.blelibs.utils;

import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.concurrent.ConcurrentLinkedDeque;

class SyzBitmapQueue<T: Serializable>(private val capacity: Int):Serializable {
    private val deque = ConcurrentLinkedDeque<T>()

    @Synchronized
    fun add(element: T) {
        while (deque.size >= capacity) {
            deque.pollFirst() // 移除队列头部的元素，因为队列是FIFO
        }
        deque.addLast(element) // 在队列尾部添加新元素
    }
    @Synchronized
    fun forEach(action: (T) -> Unit) {
        deque.forEach { element ->
            action(element)
        }
    }

    @Synchronized
    fun poll(): T {
        return deque.pollFirst() // 移除并返回队列头部的元素
    }

    @Synchronized
    fun pollMultiple(n: Int): List<T> {
        val result = mutableListOf<T>()
        repeat(n) {
            deque.pollFirst()?.let {
                result.add(it)
            }
        }
        return result
    }

    @Synchronized
    fun  add(element: T, n: Int) {
        repeat(n) {
            val copiedElement = deepCopy(element)
            deque.addLast(copiedElement) // 在队列尾部添加新元素n次
        }
    }

    private val TAG="SyzBitmapQueue>>>>"
    @Synchronized
    fun duplicateElements(times: Int) {
        if (times <= 1) {
            return // 如果复制次数为1或更少，则不需要进行任何操作
        }
        val originalElements = mutableListOf<T>()
        deque.forEach { element ->
            originalElements.add(element)
        }
        repeat(times - 1){
            originalElements.forEach { element ->
                val copiedElement = deepCopy(element)
                add(copiedElement)
            }
        }
    }

    fun <T : Serializable> deepCopy(obj: T): T {
        // 将对象写入到字节流
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.writeObject(obj)
        }

        // 从字节流中读取对象，实现深度复制
        val bais = ByteArrayInputStream(baos.toByteArray())
        ObjectInputStream(bais).use { ois ->
            @Suppress("unchecked_cast")
            return ois.readObject() as T
        }
    }

    @Synchronized
    fun isEmpty(): Boolean {
        return deque.isEmpty()
    }

    @Synchronized
    fun size(): Int {
        return deque.size
    }

    @Synchronized
    fun clear() {
        deque.clear() // 清空队列中的所有元素
    }


}