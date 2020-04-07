package com.albertsnow.demo.controller

import com.albertsnow.demo.bean.MyResponse
import com.albertsnow.demo.utils.jsonToPojo
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {

    @RequestMapping("/")
    fun home(): String {
        val context = ClassPathXmlApplicationContext("Beans.xml")
        val obj = context.getBean("response") as MyResponse
        return jsonToPojo(obj)
    }
}