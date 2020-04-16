package com.albertsnow.demo.controller

import com.albertsnow.demo.DOWNLOAD_EROOR_FILE_NOT_EXIT
import com.albertsnow.demo.DOWNLOAD_ERROR_MSG
import com.albertsnow.demo.DOWNLOAD_SUCCESS_MSG
import com.albertsnow.demo.bean.MyApk
import org.slf4j.LoggerFactory
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class DownloadApkController {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/translate.apk")
    fun home(request: HttpServletRequest, response: HttpServletResponse,
             @RequestHeader(required = false) range: String?): String {

        val context = ClassPathXmlApplicationContext("Beans.xml")
        val myApk = context.getBean("apk") as MyApk


        logger.info("start downloadFile")
        // 接口调用开始时间
        val beginTime = System.currentTimeMillis()

        var returnMsg = DOWNLOAD_SUCCESS_MSG

        // 加载文件
        val file = File(myApk.filePath)
        if (file.exists()) {
            val inputStream = FileInputStream(file)
            var os: OutputStream? = null
            try {
                //添加响应头 设置允许浏览器可尝试恢复中断的下载
                handleHeader(response, file)

                if (isPartialDownload(range)) {
                    //设置响应状态206
                    response.status = HttpServletResponse.SC_PARTIAL_CONTENT
                    //处理range 计算请求的哪部分数据
                    val split = range!!.split("bytes=|-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    var begin: Long = 0
                    if (split.size >= 2) {
                        begin = java.lang.Long.valueOf(split[1])
                    }
                    var end = file.length() - 1
                    if (split.size >= 3) {
                        end = java.lang.Long.valueOf(split[2]) - 1
                    }
                    var len = end - begin + 1
                    //如果请求的文件长度超过文件实际长度 返回错误状态
                    if (end > file.length()) {
                        //返回状态 500
                        response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                        returnMsg = "error happen 500"
                    } else {
                        //将指针移到请求片段的开始位置
                        inputStream.skip(begin)
                        //设置响应状态 206 部分内容
                        response.status = HttpServletResponse.SC_PARTIAL_CONTENT
                        //设置内容类型
                        response.contentType = request.servletContext.getMimeType(file.name)
                        //添加响应头 设置片段开始位置 结束位置 文件总长度
                        response.addHeader("Content-Range", "bytes " + begin + "-" + end + "/" + file.length())
                        //添加响应头 设置响应片段的长度
                        response.addHeader("Content-Length", len.toString())
                        os = response.outputStream
                        //从文件流中读指定长度的片段输出
                        val buf = ByteArray(1024)
                        while (len > 0) {
                            inputStream.read(buf)
                            val l = if (len > 1024) 1024 else len
                            os?.write(buf, 0, l.toInt())
                            os?.flush()
                            len -= l
                        }
                    }
                } else {
                    //设置响应状态200
                    response.status = HttpServletResponse.SC_OK
                    //设置内容类型
                    response.contentType = request.servletContext.getMimeType(file.name)
                    //添加响应头 设置内容长度
                    response.addHeader("Content-Length", file.length().toString())
                    os = response.outputStream
                    val buf = ByteArray(1024)
                    var len = inputStream.read(buf)
                    while (len != -1) {
                        os?.write(buf, 0, len)
                        os?.flush()
                        len = inputStream.read(buf)
                    }
                }
                return returnMsg
            } catch (e: Exception) {
                logger.error("下载接口异常，e={}", e)
                response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                return DOWNLOAD_ERROR_MSG
            } finally {
                //结束后释放资源
                inputStream?.close()
                os?.close()

                // 接口调用耗时
                val cost = System.currentTimeMillis() - beginTime
                logger.info("downloadFile end, times={}", cost)
            }
        } else {
            response.addHeader("Content-Disposition", "No offline package files found")
            return DOWNLOAD_EROOR_FILE_NOT_EXIT + myApk.filePath ?: "not set"
        }
    }

    private fun isPartialDownload(range: String?) = range != null && range.contains("bytes=") && range.contains("-")

    private fun handleHeader(response: HttpServletResponse, file: File) {
        response.addHeader("Accept-Ranges", "bytes")
        response.addHeader("Cache-control", "private")
        //添加响应头 设置浏览器另存为对话框的默认文件名
        response.addHeader("Content-Disposition", "filename=" + file.name)
        response.addHeader("Last-Modified", SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss Z", Locale.ENGLISH).format(file.lastModified()) + " GMT")
    }
}