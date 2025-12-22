package com.cvte.irremote.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * IR日志工具类
 * 
 * 提供统一的日志记录功能，支持:
 * - Logcat 输出
 * - 内存日志存储 (用于调试界面展示)
 * - 日志级别过滤
 */
object IRLogger {
    
    // 日志开关
    var isEnabled: Boolean = true
    
    // 最小日志级别
    var minLevel: Level = Level.DEBUG
    
    // 内存日志缓存 (最多保存100条)
    private const val MAX_LOG_SIZE = 100
    private val logBuffer = CopyOnWriteArrayList<LogEntry>()
    
    // 时间格式
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * 日志级别
     */
    enum class Level(val priority: Int, val label: String) {
        VERBOSE(Log.VERBOSE, "V"),
        DEBUG(Log.DEBUG, "D"),
        INFO(Log.INFO, "I"),
        WARN(Log.WARN, "W"),
        ERROR(Log.ERROR, "E")
    }
    
    /**
     * 日志条目
     */
    data class LogEntry(
        val timestamp: Long,
        val level: Level,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    ) {
        fun format(): String {
            val time = dateFormat.format(Date(timestamp))
            val error = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
            return "[$time] ${level.label}/$tag: $message$error"
        }
    }
    
    /**
     * Verbose 日志
     */
    fun v(tag: String, message: String) {
        log(Level.VERBOSE, tag, message)
    }
    
    /**
     * Debug 日志
     */
    fun d(tag: String, message: String) {
        log(Level.DEBUG, tag, message)
    }
    
    /**
     * Info 日志
     */
    fun i(tag: String, message: String) {
        log(Level.INFO, tag, message)
    }
    
    /**
     * Warning 日志
     */
    fun w(tag: String, message: String) {
        log(Level.WARN, tag, message)
    }
    
    /**
     * Error 日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message, throwable)
    }
    
    /**
     * 记录日志
     */
    private fun log(level: Level, tag: String, message: String, throwable: Throwable? = null) {
        if (!isEnabled || level.priority < minLevel.priority) {
            return
        }
        
        // 输出到 Logcat
        when (level) {
            Level.VERBOSE -> Log.v(tag, message, throwable)
            Level.DEBUG -> Log.d(tag, message, throwable)
            Level.INFO -> Log.i(tag, message, throwable)
            Level.WARN -> Log.w(tag, message, throwable)
            Level.ERROR -> Log.e(tag, message, throwable)
        }
        
        // 保存到内存缓冲
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )
        
        synchronized(logBuffer) {
            logBuffer.add(entry)
            // 超过最大容量时移除最早的日志
            while (logBuffer.size > MAX_LOG_SIZE) {
                logBuffer.removeAt(0)
            }
        }
    }
    
    /**
     * 获取所有日志
     */
    fun getLogs(): List<LogEntry> = logBuffer.toList()
    
    /**
     * 获取指定级别的日志
     */
    fun getLogs(level: Level): List<LogEntry> = 
        logBuffer.filter { it.level == level }
    
    /**
     * 获取指定标签的日志
     */
    fun getLogs(tag: String): List<LogEntry> = 
        logBuffer.filter { it.tag == tag }
    
    /**
     * 清除所有日志
     */
    fun clear() {
        logBuffer.clear()
    }
    
    /**
     * 导出日志为字符串
     */
    fun export(): String = logBuffer.joinToString("\n") { it.format() }
}
