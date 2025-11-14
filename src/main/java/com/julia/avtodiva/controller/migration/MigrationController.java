package com.julia.avtodiva.controller.migration;

import com.julia.avtodiva.service.migration.MigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/migrate")
public class MigrationController {
    private final MigrationService migrationService;

    @Autowired
    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/slots")
    public String migrate() {
        migrationService.migrateSlotsToNewRules();
        return "Migration finished!";
    }
}
