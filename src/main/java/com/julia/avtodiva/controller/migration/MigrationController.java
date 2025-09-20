package com.julia.avtodiva.controller.migration;

import com.julia.avtodiva.service.migration.MigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/migrate")
@RequiredArgsConstructor
public class MigrationController {
    private final MigrationService migrationService;

    @GetMapping("/slots")
    public String migrate() {
        migrationService.migrateSlotsToNewRules();
        return "Migration finished!";
    }
}
