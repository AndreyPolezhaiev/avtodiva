package com.julia.avtodiva;

import com.julia.avtodiva.ui.MainFrame;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@EnableCaching
@SpringBootApplication
public class AvtoDivaApplication {
	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");

        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		ConfigurableApplicationContext context = SpringApplication.run(AvtoDivaApplication.class, args);

		SwingUtilities.invokeLater(() -> {
			MainFrame mainFrame = context.getBean(MainFrame.class);
			mainFrame.setVisible(true);
		});
	}
}
