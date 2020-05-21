package ru.vkr.vkr;


import edu.csus.ecs.pc2.Starter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={
		"ru.vkr.vkr", "edu.csus.ecs.pc2"})
public class VkrApplication {

	public static void main(String[] args) {
		SpringApplication.run(VkrApplication.class, args);
	}
}
