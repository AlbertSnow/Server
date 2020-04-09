package com.albertsnow.demo.controller

import com.albertsnow.demo.bean.MyResponse
import com.albertsnow.demo.utils.jsonToPojo
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class HomeController {

    @RequestMapping("/1")
    fun home(request: HttpServletRequest, response: HttpServletResponse): String {
        val context = ClassPathXmlApplicationContext("Beans.xml")
        val obj = context.getBean("response") as MyResponse
        return jsonToPojo(obj)
    }
}