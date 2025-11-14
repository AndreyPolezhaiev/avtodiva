package com.julia.avtodiva;

import com.julia.avtodiva.ui.MainFrame;
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

		ConfigurableApplicationContext context = SpringApplication.run(AvtoDivaApplication.class, args);

		SwingUtilities.invokeLater(() -> {
			MainFrame mainFrame = context.getBean(MainFrame.class);
			mainFrame.setVisible(true);
		});
	}
}
