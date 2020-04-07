package com.albertsnow.demo

import com.albertsnow.demo.bean.MyResponse
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.ClassPathXmlApplicationContext



@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)

	val context = ClassPathXmlApplicationContext("Beans.xml")
	val obj = context.getBean("response") as MyResponse
	System.out.println("-----------Age: ${obj.age}")

}
