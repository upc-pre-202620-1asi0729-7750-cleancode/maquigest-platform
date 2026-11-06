package com.maquigest.platform.shared.architecture;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/** Import and fully-qualified-name guard for the six MaquiGest bounded contexts. */
class BoundedContextDependencyArchitectureTests {
    private static final Set<String> CONTEXTS = Set.of(
        "iam", "inventory", "profiles", "rentals", "maintenance", "subscriptions");
    private static final Path SOURCES = Path.of("src", "main", "java", "com", "maquigest", "platform");
    private static final Pattern REFERENCES = Pattern.compile(
        "com\\.maquigest\\.platform\\.([a-z]+)\\.([a-z]+)\\.([A-Za-z_][A-Za-z_0-9]*)");

    private static List<Path> sources() throws IOException {
        assertTrue(Files.isDirectory(SOURCES), "Missing production sources: " + SOURCES.toAbsolutePath());
        try (Stream<Path> walk = Files.walk(SOURCES)) {
            return walk.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private static String owner(Path file) {
        return SOURCES.relativize(file).getName(0).toString();
    }

    @Test void everyBoundedContextIsPresent() throws IOException {
        var found = new java.util.HashSet<String>();
        for (Path file : sources()) found.add(owner(file));
        assertTrue(found.containsAll(CONTEXTS), "Some bounded contexts are absent: " + found);
    }

    @Test void domainDoesNotReachRestInfrastructureOrOtherBoundedContexts() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : sources()) {
            String context = owner(file);
            if (!CONTEXTS.contains(context)) continue;
            Path rel = SOURCES.relativize(file);
            if (rel.getNameCount() < 2 || !"domain".equals(rel.getName(1).toString())) continue;
            Matcher refs = REFERENCES.matcher(Files.readString(file));
            while (refs.find()) {
                String dependencyContext = refs.group(1);
                String dependencyLayer = refs.group(2);
                if (CONTEXTS.contains(dependencyContext) && !dependencyContext.equals(context))
                    violations.add(rel + " -> " + refs.group());
                if (dependencyContext.equals(context) &&
                    ("interfaces".equals(dependencyLayer) || "infrastructure".equals(dependencyLayer)))
                    violations.add(rel + " -> " + refs.group());
            }
        }
        assertTrue(violations.isEmpty(), () -> "Domain inward-dependency violations:\n" + String.join("\n", violations));
    }

    @Test void crossContextUsageReferencesPublishedInterfacesOnly() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : sources()) {
            String context = owner(file);
            if (!CONTEXTS.contains(context)) continue;
            Path rel = SOURCES.relativize(file);
            Matcher refs = REFERENCES.matcher(Files.readString(file));
            while (refs.find()) {
                String target = refs.group(1);
                if (!CONTEXTS.contains(target) || context.equals(target)) continue;
                boolean published = "interfaces".equals(refs.group(2)) &&
                    ("acl".equals(refs.group(3)) || "events".equals(refs.group(3)));
                if (!published) violations.add(rel + " -> " + refs.group());
            }
        }
        assertTrue(violations.isEmpty(), () -> "Unauthorized cross-context dependencies:\n" + String.join("\n", violations));
    }

    @Test void sharedDoesNotImportSpecificBusinessContexts() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : sources()) {
            if (!"shared".equals(owner(file))) continue;
            Matcher refs = REFERENCES.matcher(Files.readString(file));
            while (refs.find()) if (CONTEXTS.contains(refs.group(1)))
                violations.add(SOURCES.relativize(file) + " -> " + refs.group());
        }
        assertTrue(violations.isEmpty(), () -> "Shared must stay context-agnostic:\n" + String.join("\n", violations));
    }
}
