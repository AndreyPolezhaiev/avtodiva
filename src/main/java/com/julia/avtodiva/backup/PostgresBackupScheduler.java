package com.julia.avtodiva.backup;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class PostgresBackupScheduler {

    private static final String PG_DUMP_PATH = "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe";
    private static final String BACKUP_DIR = "D:\\backups";
    private static final String DB_NAME = "avtodiva";
    private static final String DB_USER = "postgres";
    // PASSWORD HERE YOU CAN CHECK YOUR PASSWORD IN application.properties!!!!!
    private static final String DB_PASSWORD = "1111";
    // PASSWORD HERE YOU CAN CHECK YOUR PASSWORD IN application.properties!!!!!

    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::runBackup, 0, 1, TimeUnit.HOURS);
    }

    private void runBackup() {
        try {
            // Создание директории, если нет
            File dir = new File(BACKUP_DIR);
            if (!dir.exists() && dir.mkdirs()) {
                System.out.println("Backup directory created: " + BACKUP_DIR);
            }

            // Формируем имя файла
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
            String backupFile = BACKUP_DIR + File.separator + "avtodiva_backup_" + timestamp + ".sql";

            // Команда pg_dump
            ProcessBuilder pb = new ProcessBuilder(
                    PG_DUMP_PATH,
                    "-U", DB_USER,
                    "-d", DB_NAME,
                    "-f", backupFile
            );

            pb.environment().put("PGPASSWORD", DB_PASSWORD);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Backup created: " + backupFile);
            } else {
                System.err.println("Backup failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
