package app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication

@SpringBootApplication
@EntityScan("app")
class SpringBootAppApplication

fun main(args: Array<String>) {
    runApplication<SpringBootAppApplication>(*args)
}
