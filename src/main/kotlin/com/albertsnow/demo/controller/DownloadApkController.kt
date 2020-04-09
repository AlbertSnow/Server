package com.albertsnow.demo.controller

import com.albertsnow.demo.bean.MyApk
import org.slf4j.LoggerFactory
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class DownloadApkController {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/translate.apk")
    fun home(request: HttpServletRequest, response: HttpServletResponse) {

        val context = ClassPathXmlApplicationContext("Beans.xml")
        val myApk = context.getBean("apk") as MyApk


        logger.info("start downloadFile")
        // 接口调用开始时间
        val beginTime = System.currentTimeMillis()
        // 加载文件
        val file = File(myApk.filePath)
        if (file.exists()) {
            val inputStream = FileInputStream(file)
            var os: OutputStream? = null
            try {
                //添加响应头 设置允许浏览器可尝试恢复中断的下载
                response.addHeader("Accept-Ranges", "bytes")
                response.addHeader("Cache-control", "private")
                //添加响应头 设置浏览器另存为对话框的默认文件名
                response.addHeader("Content-Disposition", "filename=" + file.name)
                response.addHeader("Last-Modified", SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss Z", Locale.ENGLISH).format(file.lastModified()) + " GMT")
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
                    os!!.write(buf, 0, len)
                    os.flush()
                    len = inputStream.read(buf)
                }
            } catch (e: Exception) {
                logger.error("离线包下载接口异常，e={}", e)
                response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                return
            } finally {
                //结束后释放资源
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                if (os != null) {
                    try {
                        os.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        } else {
            response.addHeader("Content-Disposition", "No offline package files found")
        }
        // 接口调用耗时
        val cost = System.currentTimeMillis() - beginTime
        logger.info("downloadFile end, times={}", cost)
    }
}