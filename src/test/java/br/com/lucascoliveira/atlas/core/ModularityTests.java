package br.com.lucascoliveira.atlas.core;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void modulesRespectDeclaredBoundaries() {
        ApplicationModules.of(AtlasCoreApiApplication.class).verify();
    }
}

